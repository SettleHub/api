package org.ossfmct.projects.users.services;

import org.ossfmct.projects.security.enums.ERole;
import org.ossfmct.projects.security.identity.IdentityService;
import org.ossfmct.projects.security.models.UserDetailsImpl;
import org.ossfmct.projects.submissions.enums.ServiceOperationStatus;
import org.ossfmct.projects.submissions.models.Submission;
import org.ossfmct.projects.users.models.User;
import org.ossfmct.projects.users.repositories.UserRepository;
import org.ossfmct.projects.users.requests.UpdateContactsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UsersService {
    private static final Logger logger = LoggerFactory.getLogger(UsersService.class);

    /**
     * Interface {@link UserRepository} is a layer for accessing users table in database.
     */
    @Autowired
    UserRepository userRepository;

    public Optional<User> getUserFromAuthentication(Authentication authentication) {
        if (!authentication.isAuthenticated()) return Optional.empty();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername());
    }

    public Boolean isVerified(User user) {
        return user.getIsVerified();
    }

    public Boolean hasAnyRole(User user) {
        return user.getRoles().stream()
            .anyMatch(role ->
                role.getName().equals(ERole.ROLE_STUDENT) ||
                role.getName().equals(ERole.ROLE_TEACHER) ||
                role.getName().equals(ERole.ROLE_MODERATOR) ||
                role.getName().equals(ERole.ROLE_ADMIN));
    }

    public Boolean hasTeacherOrModeratorOrAdminRole(User user) {
        return user.getRoles().stream()
            .anyMatch(role ->
                role.getName().equals(ERole.ROLE_TEACHER) ||
                role.getName().equals(ERole.ROLE_MODERATOR) ||
                role.getName().equals(ERole.ROLE_ADMIN));
    }

    public Boolean hasModeratorOrAdminRole(User user) {
        return user.getRoles().stream()
            .anyMatch(role ->
                role.getName().equals(ERole.ROLE_MODERATOR) ||
                role.getName().equals(ERole.ROLE_ADMIN));
    }

    public Set<User> getAll() {
        return this.userRepository.findAll()
            .stream()
            .map(user -> (User) user)
            .collect(Collectors.toSet());
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserByVerificationCode(String code) {
        return userRepository.findByVerificationCode(code);
    }

    public Optional<String> getVerificationCodeByUsername(String username) {
        return userRepository.findAll().stream()
            .filter(user -> user.getUsername().equals(username))
            .map(User::getVerificationCode)
            .findFirst();
    }
    public List<User> getUserByQuery(String query) {
        return userRepository.findAll().stream()
            .filter(user -> (
                user.getId().toString().equals(query) ||
                user.getUsername().equals(query) ||
                user.getEmail().equals(query) ||
                user.getPhone().equals(query) ||
                user.getFirstName().contains(query) ||
                user.getLastName().contains(query) ||
                user.getMiddleName().contains(query) ||
                findByFullName(user.getLastName(), user.getFirstName(), user.getMiddleName()).isPresent()
            )).collect(Collectors.toList());
    }

    private Optional<User> findByFullName(String fullName) {
        return userRepository.findAll()
            .stream()
            .filter(user -> (user.getLastName() + " " + user.getFirstName() + " " + user.getMiddleName()).contains(fullName))
            .findFirst();
    }

    private Optional<User> findByFullName(String lastName, String firstName, String middleName) {
        return userRepository.findAll()
            .stream()
            .filter(user -> (user.getLastName() + " " + user.getFirstName() + " " + user.getMiddleName()).contains(lastName + firstName + middleName))
            .findFirst();
    }

    public ServiceOperationStatus add(User newUser) {
        if (isAlreadyExists(newUser)) {
            return ServiceOperationStatus.ALREADY_EXISTS;
        } else {
            userRepository.save(newUser);
            logger.info("User registered: {}", newUser);
            return ServiceOperationStatus.SUCCESSFUL;
        }
    }

    public ServiceOperationStatus update(User target, User updated) {
        if (userRepository.findById(target.getId()).isEmpty()) return ServiceOperationStatus.NOT_FOUND;
        if (!(target.getFirstSignDate().equals(updated.getFirstSignDate()))) return ServiceOperationStatus.INVALID_DATA;
        userRepository.save(updated);
        return ServiceOperationStatus.SUCCESSFUL;
    }

    public ServiceOperationStatus updateById(Long id, User updated) {
        Optional<User> optional = userRepository.findById(id);
        if (optional.isEmpty()) return ServiceOperationStatus.NOT_FOUND;
        return update(optional.get(), updated);
    }

    public ServiceOperationStatus updateContactsById(Long id, UpdateContactsRequest updatedContacts) {
        Optional<User> optional = userRepository.findById(id);
        if (optional.isEmpty()) return ServiceOperationStatus.NOT_FOUND;
        User user = optional.get();
        user.setEmail(updatedContacts.getEmail());
        user.setPhone(updatedContacts.getPhone());
        user.setBirthDate(updatedContacts.getBirthDate());
        return updateById(id, user);
    }

    public ServiceOperationStatus delete(User target) {
        Optional<User> optional = userRepository.findById(target.getId());
        if (optional.isEmpty()) return ServiceOperationStatus.NOT_FOUND;
        userRepository.delete(target);
        logger.info("Deleted {}", target);
        return ServiceOperationStatus.SUCCESSFUL;
    }

    public ServiceOperationStatus deleteById(Long id) {
        Optional<User> optional = userRepository.findById(id);
        if (optional.isEmpty()) return ServiceOperationStatus.NOT_FOUND;
        return delete(optional.get());
    }

    public boolean isAlreadyExists(User user) {
        if (user.getId() != null && getUserById(user.getId()).isPresent()) {
            return true;
        }

        if (user.getUsername() != null && getUserByUsername(user.getUsername()).isPresent()) {
            return true;
        }

        if (user.getEmail() != null && getUserByEmail(user.getEmail()).isPresent()) {
            return true;
        }

        if (user.getFirstName() != null && !user.getFirstName().isEmpty() &&
            user.getLastName() != null && !user.getLastName().isEmpty() &&
            user.getMiddleName() != null && !user.getMiddleName().isEmpty() &&
            findByFullName(user.getLastName(), user.getFirstName(), user.getMiddleName()).isPresent()
        ) {
            return true;
        }

        return false;
    }
}