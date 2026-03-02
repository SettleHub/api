package org.ossfmct.projects.security.identity;

import org.apache.commons.lang3.tuple.Pair;
import org.ossfmct.projects.security.enums.ERole;
import org.ossfmct.projects.security.identity.requests.SignInRequest;
import org.ossfmct.projects.security.identity.requests.SignUpRequest;
import org.ossfmct.projects.security.identity.requests.SignUpRequestCompleteWithEmail;
import org.ossfmct.projects.security.identity.requests.UpdatePasswordRequest;
import org.ossfmct.projects.security.identity.responses.JwtResponse;
import org.ossfmct.projects.security.jwt.JwtService;
import org.ossfmct.projects.security.models.Role;
import org.ossfmct.projects.security.models.UserDetailsImpl;
import org.ossfmct.projects.submissions.enums.ServiceOperationStatus;
import org.ossfmct.projects.tools.CodeGenerator;
import org.ossfmct.projects.users.models.BlankUser;
import org.ossfmct.projects.users.models.User;
import org.ossfmct.projects.users.repositories.BlankUserRepository;
import org.ossfmct.projects.users.repositories.RoleRepository;
import org.ossfmct.projects.users.services.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IdentityService {
    private static final Logger logger = LoggerFactory.getLogger(IdentityService.class);

    /**
     * Interface {@link AuthenticationManager} provide user authorization by using username and password credentials.
     */
    @Autowired
    AuthenticationManager authenticationManager;

    /**
     * Interface {@link PasswordEncoder} using for hashing user special authorization phrases (simply passwords).
     */
    @Autowired
    PasswordEncoder encoder;

    /**
     * Interface {@link RoleRepository} is a layer for accessing roles table in database.
     */
    @Autowired
    RoleRepository roleRepository;

    /**
     * Interface {@link BlankUserRepository} is a layer for accessing blank_users table in database.
     */
    @Autowired
    BlankUserRepository blankUserRepository;

    /**
     * {@link JwtService} provide a JWT Token generating, validating and others methods.
     */
    @Autowired
    JwtService jwtService;

    /**
     * {@link UsersService} provide users data managing methods.
     */
    @Autowired
    UsersService usersService;

    public JwtResponse authenticate(SignInRequest signInRequest) {
        Authentication authentication = authenticationManager
            .authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtService.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
            .collect(Collectors.toList());

        return new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles);
    }

    public ServiceOperationStatus register(SignUpRequest signUpRequest) {
        Optional<User> optionalUser = usersService.getUserByUsername(signUpRequest.getUsername());
        if (optionalUser.isPresent()) {
            return ServiceOperationStatus.ALREADY_EXISTS;
        }
        User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(), encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        logger.info("New user requested roles: {}", strRoles.toString());
        Set<Role> roles = new HashSet<>();

        if (strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(roleName -> {
                try {
                    Role role = roleRepository.findByName(ERole.valueOf(roleName))
                        .orElseThrow(() -> {
                            logger.error("Requested role {} is not found.", roleName);
                            return new RuntimeException("Error: Role not found.");
                        });
                    roles.add(role);
                } catch (IllegalArgumentException e) {
                    logger.error("Requested role {} is invalid.", roleName);
                }
            });
        }

        user.setRoles(roles);
        return usersService.add(user);
    }

    public ServiceOperationStatus register(SignUpRequestCompleteWithEmail signUpRequest) {
        Optional<BlankUser> optionalBlankUser = blankUserRepository.findByEmail(signUpRequest.getEmail());
        if (optionalBlankUser.isEmpty()) {
            return ServiceOperationStatus.NOT_FOUND;
        }
        BlankUser blankUser = optionalBlankUser.get();
        if (blankUser.getIsAlreadyRegistered() || usersService.getUserByUsername(signUpRequest.getEmail()).isPresent()) {
            return ServiceOperationStatus.ALREADY_EXISTS;
        }
        User user = new User(signUpRequest.getEmail(), signUpRequest.getEmail(), encoder.encode(signUpRequest.getPassword()));
        user.setFirstName(blankUser.getFirstName());
        user.setLastName(blankUser.getLastName());
        user.setMiddleName(blankUser.getMiddleName());
        user.setCourse(blankUser.getCourse());
        user.setPhone(blankUser.getPhone());
        Set<Role> roles = new HashSet<>();
        user.setRoles(roles);
        user.setIsVerified(false);
        user.setVerificationCode(
            CodeGenerator.generateVerificationCode()
        );
        return usersService.add(user);
    }

    public ServiceOperationStatus verify(String verificationCode) {
        Optional<User> optionalUser = usersService.getUserByVerificationCode(verificationCode);
        if (optionalUser.isEmpty()) {
            return ServiceOperationStatus.NOT_FOUND;
        }
        User user = optionalUser.get();

        if (user.getVerificationCode().equals(verificationCode)) {
            logger.info("New user requested role: {}", ERole.ROLE_STUDENT.toString());
            Set<Role> roles = new HashSet<>();
            roles.add(
                roleRepository.findByName(ERole.ROLE_STUDENT)
                    .orElseThrow(() -> {
                        logger.error("Requested role {} is not found.", ERole.ROLE_STUDENT.toString());
                        return new RuntimeException("Error: Role not found.");
                    })
            );
            logger.info("New user assigned role: {}", ERole.ROLE_STUDENT.toString());

            Optional<BlankUser> optionalBlankUser = blankUserRepository.findByEmail(user.getUsername());
            if (optionalBlankUser.isPresent()) {
                BlankUser blankUser = optionalBlankUser.get();
                blankUser.setIsAlreadyRegistered(true);
                blankUserRepository.save(blankUser);
            }
            user.setRoles(roles);
            user.setIsVerified(true);
            return usersService.updateById(user.getId(), user);
        } else {
            return ServiceOperationStatus.UPDATE_FAILED;
        }
    }

    public Pair<ServiceOperationStatus, String> forgotPassword(String username) {
        Optional<User> optionalUser = usersService.getUserByUsername(username);
        if (optionalUser.isEmpty()) {
            return Pair.of(ServiceOperationStatus.NOT_FOUND, "");
        }
        User user = optionalUser.get();

        String code = CodeGenerator.generateResetPasswordCode();
        user.setResetPasswordCode(code);
        return Pair.of(usersService.updateById(user.getId(), user), code);
    }

    public ServiceOperationStatus verifyForgotPasswordCode(String username, String code) {
        Optional<User> optionalUser = usersService.getUserByUsername(username);
        if (optionalUser.isEmpty()) {
            return ServiceOperationStatus.NOT_FOUND;
        }
        User user = optionalUser.get();

        String existsCode = user.getResetPasswordCode();
        if (!existsCode.isEmpty() && existsCode.equals(code)) {
            return ServiceOperationStatus.SUCCESSFUL;
        }
        return ServiceOperationStatus.INVALID_DATA;
    }

    public ServiceOperationStatus updatePasswordByResetPasswordCode(UpdatePasswordRequest request) {
        if (request.getUsername().isEmpty()
            || request.getPassword().isEmpty()
            || request.getCode().isEmpty())
        {
            return ServiceOperationStatus.INVALID_DATA;
        }

        Optional<User> optionalUser = usersService.getUserByUsername(request.getUsername());
        if (optionalUser.isEmpty()) {
            return ServiceOperationStatus.NOT_FOUND;
        }
        User user = optionalUser.get();
        if (!user.getResetPasswordCode().equals(request.getCode())) {
            return ServiceOperationStatus.INVALID_DATA;
        }
        user.setPassword(encoder.encode(request.getPassword()));
        return usersService.updateById(user.getId(), user);
    }
}
