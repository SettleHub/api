package org.ossfmct.projects.security.identity;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.apache.commons.lang3.tuple.Pair;
import org.ossfmct.projects.mail.MailConfig;
import org.ossfmct.projects.mail.enums.MailStatus;
import org.ossfmct.projects.mail.models.EmailDetails;
import org.ossfmct.projects.mail.service.EmailService;
import org.ossfmct.projects.security.identity.requests.SignInRequest;
import org.ossfmct.projects.security.identity.requests.SignUpRequest;
import org.ossfmct.projects.security.identity.requests.SignUpRequestCompleteWithEmail;
import org.ossfmct.projects.security.identity.requests.UpdatePasswordRequest;
import org.ossfmct.projects.security.identity.responses.JwtResponse;
import org.ossfmct.projects.security.identity.responses.MessageResponse;
import org.ossfmct.projects.security.identity.responses.WhoAmIResponse;
import org.ossfmct.projects.security.models.UserDetailsImpl;
import org.ossfmct.projects.submissions.enums.ServiceOperationStatus;
import org.ossfmct.projects.tools.interfaces.IResourceLoaderService;
import org.ossfmct.projects.tools.resources.ResourceLoaderService;
import org.ossfmct.projects.users.models.User;
import org.ossfmct.projects.users.services.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Provide the endpoints to user authorization and authentication
 * by using JWT tokens, user and role repositories, password encoder and authentication manager.
 */
@RestController
@RequestMapping("/identity")
public class IdentityController {
    private static final Logger logger = LoggerFactory.getLogger(IdentityController.class);

    @Value("${mail.verification.letter}")
    private String USER_VERIFICATION_LETTER_FILENAME;

    @Value("${mail.reset-password.letter}")
    private String USER_RESET_PASSWORD_LETTER_FILENAME;

    @Value("${mail.template-letter}")
    private String TEMPLATE_LETTER_FILENAME;

    /**
     * {@link IdentityService} provide identity data managing methods.
     */
    @Autowired
    IdentityService identityService;

    /**
     * {@link UsersService} provide users data managing methods.
     */
    @Autowired
    UsersService usersService;

    /**
     * {@link EmailService} provide email sending methods.
     */
    @Autowired
    EmailService emailService;

    /**
     * {@link ResourceLoaderService} provide loading resource files methods.
     */
    @Autowired
    IResourceLoaderService resourceLoaderService;

    /**
     * Endpoint for user authorization in service for accessing additional rights.
     * @param signInRequest is a {@link SignInRequest} object.
     * @return Response is a {@link JwtResponse} object which contains JWT Token for Bearer authentication and some user credentials.
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody SignInRequest signInRequest) {
        Optional<User> optionalUser = usersService.getUserByUsername(signInRequest.getUsername());
        if (optionalUser.isEmpty()) {
            logger.warn("Attempt to login when user does not found.");
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("User not found.");
        }
        User user = optionalUser.get();
        if (user.getIsVerified() == true) {
            logger.info("User logged in: {}", user.toString());
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(identityService.authenticate(signInRequest));
        } else {
            logger.warn("Attempt to login when user does not verified: {}", user.toString());
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("User doesn't verified.");
        }
    }

    /**
     * Endpoint for user registration. User credentials will be saving if all conditions are met.
     * @param signUpRequest is a {@link SignUpRequest} object.
     * @return Response is a simple {@link MessageResponse} object.
     */
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        ServiceOperationStatus status = identityService.register(signUpRequest);
        if (status.equals(ServiceOperationStatus.SUCCESSFUL)) {
            return ResponseEntity.status(HttpStatus.OK).body("User registered successfully.");
        } else if (status.equals(ServiceOperationStatus.ALREADY_EXISTS)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists.");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("STATUS: " + status);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @PostMapping(value = "/signup/student")
    public ResponseEntity<?> registerStudentWithEmail(@RequestBody SignUpRequestCompleteWithEmail request)
            throws MailException, MessagingException, IOException {
        ServiceOperationStatus status = identityService.register(request);

        if (status == ServiceOperationStatus.SUCCESSFUL) {
            InputStream stream = resourceLoaderService.getInputStreamFromResourceFile(USER_VERIFICATION_LETTER_FILENAME);
            String template = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

            Optional<String> optionalCode = usersService.getVerificationCodeByUsername(request.getEmail());
            String code = optionalCode.get();
            String finalHtml = template.replace("VERIFICATION_CODE", code);
            EmailDetails letter = new EmailDetails();
            letter.setMsgBody(finalHtml);

            emailService.sendVerificationMail(letter, request.getEmail());
            return ResponseEntity.ok("User registered successfully. Please verify your email.");
        } else if (status == ServiceOperationStatus.ALREADY_EXISTS) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists.");
        } else if (status == ServiceOperationStatus.NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Blank user not found.");
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("STATUS: " + status);
    }

    /**
     * Endpoint for verification user information and privileges by Bearer authentication.
     * @param authentication is a Bearer authentication with JWT token.
     * @param isJson what the response will look like
     * @return {@link WhoAmIResponse} object or simple {@link String} which contains short user information.
    */
    @GetMapping("/whoami")
    public ResponseEntity<?> whoAmI(Authentication authentication,
        @RequestParam(value = "json", required = false, defaultValue = "false") boolean isJson) {
        return defineUserInformation(authentication, isJson);
    }

    /**
     * In method described logic of getting user object from {@link AuthenticationManager}.
     */
    private ResponseEntity<?> defineUserInformation(Authentication authentication, boolean isJson) {
        if (!authentication.isAuthenticated()) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("Authentication doesn't valid."));
        }

        Optional<User> optional = usersService.getUserFromAuthentication(authentication);
        if (optional.isEmpty()) {
            logger.error("Could not find user by authentication details. UserDetails: {};",  ((UserDetailsImpl) authentication.getPrincipal()).toString());
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse("Could not find user by authentication details."));
        }
        User user = optional.get();
        WhoAmIResponse response = new WhoAmIResponse(user);

        if (isJson) {
            return ResponseEntity.ok(response);
        } else {
            String simpleResponse = "User name: " + response.getUsername() + "\n" +
                "Email: " + response.getEmail() + "\n" +
                "Full Name: " + response.getFullName() + "\n" +
                "Roles: " + response.getRoles() + "\n" +
                "Last signed: " + response.getLastSignDate();

            return ResponseEntity.ok(simpleResponse);
        }
    }

    @GetMapping(value = "/verify", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<?> verifyUser(@RequestParam("code") String code) throws IOException {
        ServiceOperationStatus status = identityService.verify(code);

        InputStream stream = resourceLoaderService.getInputStreamFromResourceFile(TEMPLATE_LETTER_FILENAME);
        String template = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        String message = "<p>Вітаємо</p>\n"
                        +"<p>Вашу електронну пошту було успішно вирифіковано.</p>";
        String finalHtml = template.replace("AREA_FOR_PLACING_TEXT", message);

        if (status == ServiceOperationStatus.SUCCESSFUL) {
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.TEXT_HTML)
                    .body(finalHtml);
        } else if (status == ServiceOperationStatus.INVALID_DATA) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Verification failed: invalid user data.");
        } else if (status == ServiceOperationStatus.NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Verification failed: user not found.");
        } else if (status == ServiceOperationStatus.UPDATE_FAILED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Verification failed: incorrect code.");
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("STATUS: " + status);
    }

    @PostMapping(value = "/forgotPassword")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String username) throws IOException, MessagingException {
        Pair<ServiceOperationStatus, String> status = identityService.forgotPassword(username);

        if (status.getLeft() == ServiceOperationStatus.SUCCESSFUL) {
            logger.info("Reset password request: (username: {};)", username);
            InputStream stream = resourceLoaderService.getInputStreamFromResourceFile(USER_RESET_PASSWORD_LETTER_FILENAME);
            String template = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

            String code = status.getRight();
            String firstFourDigits = code.substring(0, 4);
            String secondFourDigits = code.substring(4);
            String finalHtml = template
                .replace("CODE_FIRST_FOUR_DIGITS", firstFourDigits)
                .replace("CODE_SECOND_FOUR_DIGITS", secondFourDigits);

            EmailDetails letter = new EmailDetails();
            letter.setMsgBody(finalHtml);

            MailStatus emailStatus = emailService.sendResetPasswordMail(letter, username);
            if (emailStatus == MailStatus.SENT) {
                return ResponseEntity.status(HttpStatus.OK).body("Successfully sent reset password code to email.");
            } else {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body("The code was generated, but we couldn't send it via email.");
            }
        } else if (status.getLeft() == ServiceOperationStatus.INVALID_DATA) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Reset password failed: invalid user data.");
        } else if (status.getLeft() == ServiceOperationStatus.NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reset password failed: user not found.");
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("STATUS: " + status);
    }

    @PostMapping(value = "/forgotPassword/verify")
    public ResponseEntity<?> verifyForgotPasswordCode(@RequestParam("email") String username,
                                                      @RequestParam("code") String code) {
        logger.info("Forgot password code verification request: (username: {}; code: {};)", username, code);
        ServiceOperationStatus status = identityService.verifyForgotPasswordCode(username, code);
        if (status == ServiceOperationStatus.SUCCESSFUL) {
            return ResponseEntity.status(HttpStatus.OK).body("Code verification was successful.");
        } else if (status == ServiceOperationStatus.INVALID_DATA) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Code verification check failed: invalid code.");
        } else if (status == ServiceOperationStatus.NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Code verification check failed: user not found.");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("STATUS: " + status);
    }

    @PostMapping(value = "/forgotPassword/update")
    public ResponseEntity<?> updatePasswordByResetPasswordCode(@RequestBody UpdatePasswordRequest request) {
        logger.info("Reset forgot password request: (username: {}; code: {};)", request.getUsername(), request.getCode());
        ServiceOperationStatus status = identityService.updatePasswordByResetPasswordCode(request);
        if (status == ServiceOperationStatus.SUCCESSFUL) {
            return ResponseEntity.status(HttpStatus.OK).body("Successfully reset password.");
        } else if (status == ServiceOperationStatus.INVALID_DATA) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reset forgot password failed: request contains invalid data.");
        } else if (status == ServiceOperationStatus.NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reset forgot password failed: user not found.");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("STATUS: " + status);
    }
}