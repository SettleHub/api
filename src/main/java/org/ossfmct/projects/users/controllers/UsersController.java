package org.ossfmct.projects.users.controllers;

import java.util.*;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.ossfmct.projects.security.identity.IdentityService;
import org.ossfmct.projects.security.identity.requests.SignUpRequest;
import org.ossfmct.projects.security.models.Role;
import org.ossfmct.projects.security.models.UserDetailsImpl;
import org.ossfmct.projects.submissions.enums.ServiceOperationStatus;
import org.ossfmct.projects.users.models.User;
import org.ossfmct.projects.users.requests.UpdateContactsRequest;
import org.ossfmct.projects.users.services.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UsersController {
    private final Logger logger = LoggerFactory.getLogger(UsersController.class);

    private final UsersService service;

    /**
     * {@link IdentityService} provide identity data managing methods.
     */
    @Autowired
    IdentityService identityService;
    private UsersService usersService;

    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<Set<User>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(service.getAll());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @GetMapping("/get")
    public ResponseEntity<?> getUserById(Authentication authentication,
                                         @RequestParam(value = "id", required = false) Long id,
                                         @RequestParam(value = "username", required = false) String username,
                                         @RequestParam(value = "email", required = false) String email) {
        Optional<User> optAuthUser = service.getUserFromAuthentication(authentication);
        if (optAuthUser.isEmpty()) {
            logger.warn("Access denied for {}", ((UserDetailsImpl) authentication.getPrincipal()).toString());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Compromised user.");
        }
        User authUser = optAuthUser.get();
        if (authUser.getId().equals(id) || service.hasTeacherOrModeratorOrAdminRole(authUser)) {
            Optional<User> optUser;
            if (id != null) {
                optUser = service.getUserById(id);
            } else if (username != null) {
                optUser = service.getUserByUsername(username);
            } else if (email != null) {
                optUser = service.getUserByEmail(email);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid data.");
            }

            if (optUser.isEmpty()) {
                logger.info("User not found by:{id={}, username={}, email={}}", id, username, email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found by:{id="+id+", username="+username+", email="+email+"}");
            } else {
                User user = optUser.get();
                return ResponseEntity.status(HttpStatus.OK).body(user);
            }
        } else {
            logger.warn("Access denied for {}", authUser);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<?> getUserByQuery(@RequestParam("query") String query) {
        List<User> queriedUsers = service.getUserByQuery(query);
        if (queriedUsers.isEmpty()) {
            logger.info("Users by query={} not found.", query);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Users by query=" + query + " not found.");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(queriedUsers);
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody User newUser) {
        Set<String> strRoles = newUser.getRoles().stream()
            .map(role -> role.getName().toString())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        ServiceOperationStatus status = identityService.register(
            new SignUpRequest(newUser.getUsername(), newUser.getEmail(), newUser.getPassword(), strRoles)
        );

        if (status.equals(ServiceOperationStatus.SUCCESSFUL)) {
            User registeredUser = usersService.getUserByUsername(newUser.getUsername()).get();
            refillPersonalData(registeredUser, newUser);
            ServiceOperationStatus updateStatus = usersService.updateById(registeredUser.getId(), registeredUser);
            if (updateStatus.equals(ServiceOperationStatus.SUCCESSFUL)) {
                return ResponseEntity.status(HttpStatus.OK).body("User added successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("STATUS:" + status);
            }
        } else if (status.equals(ServiceOperationStatus.ALREADY_EXISTS)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("STATUS:" + status);
        }
    }

    private void refillPersonalData(User registered, User templateUser) {
        registered.setFirstName(templateUser.getFirstName());
        registered.setMiddleName(templateUser.getMiddleName());
        registered.setLastName(templateUser.getLastName());
        registered.setBirthDate(templateUser.getBirthDate());
        registered.setPhotoUrl(templateUser.getPhotoUrl());;
        registered.setAvatarUrl(templateUser.getAvatarUrl());
        registered.setPhone(templateUser.getPhone());
    }

    private record UserOperationResult(User user, ServiceOperationStatus status) { }

    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @PostMapping("/add/many")
    public ResponseEntity<?> addMany(@RequestBody Set<User> users) {
        for (User newUser : users) {
            if (service.isAlreadyExists(newUser)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exist. " + newUser.toString());
            }
        }

        List<UserOperationResult> results = new ArrayList<>();
        for (User newUser : users) {
            Set<String> strRoles = newUser.getRoles().stream()
                    .map(role -> role.getName().toString())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            ServiceOperationStatus status = identityService.register(
                    new SignUpRequest(newUser.getUsername(), newUser.getEmail(), newUser.getPassword(), strRoles));
            Optional<User> registeredUser = usersService.getUserByUsername(newUser.getUsername());
            if (status.equals(ServiceOperationStatus.SUCCESSFUL) && registeredUser.isPresent()) {
                refillPersonalData(registeredUser.get(), newUser);
            }
            results.add(new UserOperationResult(newUser, status));
        }
        boolean allSuccessful = results.stream()
            .allMatch(result -> result.status.equals(ServiceOperationStatus.SUCCESSFUL));
        HttpStatus responseStatus = allSuccessful ? HttpStatus.OK : HttpStatus.MULTI_STATUS;

        return ResponseEntity.status(responseStatus).body(results);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<?> updateById(@RequestParam("id") Long id, @RequestBody User updated) {
        ServiceOperationStatus status = service.updateById(id, updated);
        if (status.equals(ServiceOperationStatus.SUCCESSFUL)) {
            return ResponseEntity.status(HttpStatus.OK).body("User updated successfully.");
        } else if (status.equals(ServiceOperationStatus.NOT_FOUND)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } else if (status.equals(ServiceOperationStatus.INVALID_DATA)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid data.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("STATUS:" + status);
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @PutMapping("/update-contacts")
    public ResponseEntity<?> updateContactsById(Authentication authentication,
                                                @RequestParam("id") Long id,
                                                @RequestBody UpdateContactsRequest updatedContacts)
    {
        Optional<User> optAuthUser = service.getUserFromAuthentication(authentication);
        if (optAuthUser.isEmpty()) {
            logger.warn("Access denied for {}", ((UserDetailsImpl) authentication.getPrincipal()).toString());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Compromised user.");
        }
        User authUser = optAuthUser.get();
        if (authUser.getId().equals(id) || service.hasTeacherOrModeratorOrAdminRole(authUser)) {
            ServiceOperationStatus status = service.updateContactsById(id, updatedContacts);
            if (status.equals(ServiceOperationStatus.SUCCESSFUL)) {
                return ResponseEntity.status(HttpStatus.OK).body("User contacts updated successfully.");
            } else if (status.equals(ServiceOperationStatus.NOT_FOUND)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            } else if (status.equals(ServiceOperationStatus.INVALID_DATA)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid data.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("STATUS:" + status);
            }
        } else {
            logger.warn("Access denied for {}", authUser);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteById(@RequestParam("id") Long id) {
        ServiceOperationStatus status = service.deleteById(id);
        if (status.equals(ServiceOperationStatus.SUCCESSFUL)) {
            return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully.");
        } else if (status.equals(ServiceOperationStatus.NOT_FOUND)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("STATUS: " + status);
        }
    }
}
