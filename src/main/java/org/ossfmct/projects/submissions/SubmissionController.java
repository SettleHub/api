package org.ossfmct.projects.submissions;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.ossfmct.projects.files.FileAccessService;
import org.ossfmct.projects.mail.models.EmailDetails;
import org.ossfmct.projects.mail.service.EmailService;
import org.ossfmct.projects.security.models.UserDetailsImpl;
import org.ossfmct.projects.spreadsheets.enums.Gender;
import org.ossfmct.projects.submissions.enums.ServiceOperationStatus;
import org.ossfmct.projects.submissions.enums.SubmissionStatus;
import org.ossfmct.projects.submissions.enums.SubmissionType;
import org.ossfmct.projects.submissions.models.Submission;
import org.ossfmct.projects.submissions.requests.SubmissionRequest;
import org.ossfmct.projects.submissions.services.SubmissionService;
import org.ossfmct.projects.tools.interfaces.IResourceLoaderService;
import org.ossfmct.projects.users.models.User;
import org.ossfmct.projects.users.services.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/submissions/")
public class SubmissionController {
    private final Logger logger = LoggerFactory.getLogger(SubmissionController.class);

    @Value("${mail.template-letter}")
    private String TEMPLATE_LETTER_FILENAME;

    private final SubmissionService submissionService;

    private final UsersService usersService;

    private final FileAccessService fileAccessService;

    private final EmailService emailService;

    private final IResourceLoaderService resourceLoaderService;

    private final SubmissionsConfig config;

    public SubmissionController(SubmissionService submissionService, UsersService usersService, FileAccessService fileAccessService, EmailService emailService, IResourceLoaderService resourceLoaderService, SubmissionsConfig config) {
        this.submissionService = submissionService;
        this.usersService = usersService;
        this.fileAccessService = fileAccessService;
        this.emailService = emailService;
        this.resourceLoaderService = resourceLoaderService;
        this.config = config;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @PostMapping(value = "add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createSubmission(Authentication authentication,
                                            @RequestPart("data") SubmissionRequest request,
                                            @RequestPart("submitterDocuments") List<MultipartFile> files
    ) {
        Optional<User> optionalUser = usersService.getUserFromAuthentication(authentication);
        if (optionalUser.isEmpty()) {
            return accessDeniedForCompromisedUser(authentication);
        }
        User authUser = optionalUser.get();
        Optional<User> optSubmissionUser = usersService.getUserById(request.getSubmitterId());
        if (optSubmissionUser.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid data.");
        User submissionUser = optSubmissionUser.get();
        if (submissionService.countOfActiveSubmissionsBySubmitter(submissionUser) >= 3) return ResponseEntity
                                                                                                .status(HttpStatus.TOO_MANY_REQUESTS)
                                                                                                .body("The maximum number of active applications has been reached.");
        Submission newSubmission = new Submission(
            submissionUser,
            request.getPhones(),
            request.getEmails(),
            request.getType(),
            request.getDescription(),
            request.getHostel(),
            request.getFloor(),
            request.getSection(),
            request.getBlock(),
            Gender.fromString(request.getBlockGender()),
            request.getRoom()
        );
        newSubmission.changeStatus(SubmissionStatus.NEEDS_REVISION);

        if (usersService.hasTeacherOrModeratorOrAdminRole(authUser) || newSubmission.getSubmitter().equals(authUser)) {
            ServiceOperationStatus submissionStatus = submissionService.add(newSubmission);

            if (submissionStatus == ServiceOperationStatus.ALREADY_EXISTS) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Submission already exists.");
            }

            if (submissionStatus != ServiceOperationStatus.SUCCESSFUL) {
                logger.error("Error while processing submission: {}", newSubmission);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Submission creation failed: " + submissionStatus);
            }

            ServiceOperationStatus fileStatus = fileAccessService.save(files, newSubmission.getSubmitter(), newSubmission);

            if (fileStatus == ServiceOperationStatus.NOT_ALLOWED_OPERATION) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to upload files.");
            }

            if (fileStatus == ServiceOperationStatus.MULTI_STATUS_OPERATION) {
                logger.warn("Some files failed to upload for submission: {}", newSubmission);
                return ResponseEntity.status(HttpStatus.MULTI_STATUS).body("Some files were not uploaded.");
            }

            if (fileStatus != ServiceOperationStatus.SUCCESSFUL) {
                logger.error("File upload failed for submission: {}", newSubmission);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + fileStatus);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Submission and files successfully uploaded.");
        } else {
            logger.warn("Access denied for {}", authUser.toString());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @GetMapping("get/all")
    public ResponseEntity<?> getAll(Authentication authentication) {
        if (usersService.getUserFromAuthentication(authentication).isEmpty()) {
            return accessDeniedForCompromisedUser(authentication);
        }
        return ResponseEntity.status(HttpStatus.OK).body(submissionService.getAll());
    }

    /**
     * Endpoint to get submissions
     *
     * @param submitter_id   id of submitter which submissions we want to get from db.
     * @param authentication {@link Authentication} object which contains auth user data
     * @return status 200 return data, others only message with status of operation
     */
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @GetMapping("get/allBySubmitter")
    public ResponseEntity<?> getAllByOwner(@RequestParam("submitter_id") Long submitter_id, Authentication authentication) {
        Optional<User> optAuthUser = usersService.getUserFromAuthentication(authentication);
        if (optAuthUser.isEmpty()) {
            return accessDeniedForCompromisedUser(authentication);
        }
        User authUser = optAuthUser.get();
        Optional<User> optSubmitter = usersService.getUserById(submitter_id);
        if (optSubmitter.isPresent()) {
            User submitter = optSubmitter.get();
            if (usersService.hasTeacherOrModeratorOrAdminRole(authUser) || authUser.equals(submitter)) {
                List<Submission> submissions = submissionService.findBySubmitter(submitter);
                if (!submissions.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.OK).body(submissions);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Submissions not found.");
                }
            }
        }
        logger.warn("Access denied for {}", authUser);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied.");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @GetMapping("get/activeCount")
    public ResponseEntity<?> getActiveCountByOwner(@RequestParam("submitter_id") Long submitter_id, Authentication authentication) {
        Optional<User> optAuthUser = usersService.getUserFromAuthentication(authentication);
        if (optAuthUser.isEmpty()) {
            return accessDeniedForCompromisedUser(authentication);
        }
        User authUser = optAuthUser.get();
        Optional<User> optSubmitter = usersService.getUserById(submitter_id);
        if (optSubmitter.isPresent()) {
            User submitter = optSubmitter.get();
            if (usersService.hasTeacherOrModeratorOrAdminRole(authUser) || authUser.equals(submitter)) {
                return ResponseEntity.status(HttpStatus.OK)
                    .body(submissionService.countOfActiveSubmissionsBySubmitter(submitter));
            }
        }
        logger.warn("Access denied for {}", authUser);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied.");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @GetMapping("get")
    public ResponseEntity<?> get(Authentication authentication, @RequestParam("number") Long number) {
        Optional<User> optUser = usersService.getUserFromAuthentication(authentication);
        if (optUser.isEmpty()) {
            return accessDeniedForCompromisedUser(authentication);
        }
        User user = optUser.get();
        Optional<Submission> optSubmission = submissionService.findByNumber(number);
        if (optSubmission.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Submission not found.");
        Submission submission = optSubmission.get();

        boolean hasHigherRole = usersService.hasTeacherOrModeratorOrAdminRole(user);
        if (submission.getSubmitter().equals(user) || hasHigherRole) {
            return ResponseEntity.status(HttpStatus.OK).body(submission);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Submission not found.");
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MODERATOR', 'ROLE_ADMIN')")
    @DeleteMapping("file/delete")
    public ResponseEntity<?> deleteFileFrom(Authentication authentication,
                                            @RequestParam("submission_number") Long submissionNumber,
                                            @RequestParam("file_id") Long targetFileId)
    {
        Optional<User> optUser = usersService.getUserFromAuthentication(authentication);
        if (optUser.isEmpty()) {
            return accessDeniedForCompromisedUser(authentication);
        }
        User user = optUser.get();
        if (usersService.hasModeratorOrAdminRole(user)) {
            Optional<Submission> optSubmission = submissionService.findByNumber(submissionNumber);
            if (optSubmission.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Submission not found.");
            Submission submission = optSubmission.get();

            ServiceOperationStatus deletionStatus = fileAccessService.delete(user, targetFileId, submission);

            if (deletionStatus == ServiceOperationStatus.NOT_ALLOWED_OPERATION) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to delete this file.");
            }

            if (deletionStatus == ServiceOperationStatus.NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found in submission.");
            }

            if (deletionStatus == ServiceOperationStatus.CREATION_FAILED) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File deletion failed.");
            }

            submission.getSubmitterDocuments().removeIf(file -> file.getId().equals(targetFileId));
            ServiceOperationStatus updateStatus = submissionService.updateByNumber(submissionNumber, submission);

            if (updateStatus != ServiceOperationStatus.SUCCESSFUL) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File deleted, but submission update failed.");
            }

            return ResponseEntity.ok("File deleted successfully.");
        } else {
            logger.warn("Access denied for {}", user);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MODERATOR', 'ROLE_ADMIN')")
    @PostMapping(value = "file/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addFileTo(Authentication authentication,
                                            @RequestParam("submission_number") Long submissionNumber,
                                            @RequestPart("submitterDocuments") List<MultipartFile> files)
    {
        Optional<User> optUser = usersService.getUserFromAuthentication(authentication);
        if (optUser.isEmpty()) {
            return accessDeniedForCompromisedUser(authentication);
        }
        User user = optUser.get();
        if (usersService.hasModeratorOrAdminRole(user)) {
            Optional<Submission> optSubmission = submissionService.findByNumber(submissionNumber);
            if (optSubmission.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Submission not found.");
            Submission submission = optSubmission.get();

            ServiceOperationStatus fileStatus = fileAccessService.save(files, submission.getSubmitter(), submission);

            if (fileStatus == ServiceOperationStatus.NOT_ALLOWED_OPERATION) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to upload files.");
            }

            if (fileStatus == ServiceOperationStatus.MULTI_STATUS_OPERATION) {
                logger.warn("Some files failed to upload for submission: {}", submission);
                return ResponseEntity.status(HttpStatus.MULTI_STATUS).body("Some files were not uploaded.");
            }

            if (fileStatus != ServiceOperationStatus.SUCCESSFUL) {
                logger.error("File upload failed for submission: {}", submission);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + fileStatus);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Submission and files successfully uploaded.");
        } else {
            logger.warn("Access denied for {}", user);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @PutMapping("update")
    public ResponseEntity<?> update(Authentication authentication, @RequestParam("number") Long targetNumber, @RequestBody Submission updated) {
        Optional<User> optUser = usersService.getUserFromAuthentication(authentication);
        if (optUser.isEmpty()) {
            return accessDeniedForCompromisedUser(authentication);
        }
        User user = optUser.get();
        if (usersService.hasTeacherOrModeratorOrAdminRole(user)) {
            Optional<Submission> optTargetSubmission = submissionService.findByNumber(targetNumber);
            if (optTargetSubmission.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Submission not found.");
            Submission target = optTargetSubmission.get();

            ServiceOperationStatus status = submissionService.update(target, updated);
            if (status.equals(ServiceOperationStatus.SUCCESSFUL)) {
                logger.info("Updated: (OLD: {}; NEW: {};)", target, updated);
                return ResponseEntity.status(HttpStatus.OK).body("Updated successfully.");
            } else if (status.equals(ServiceOperationStatus.NOT_FOUND)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Submission not found.");
            } else if (status.equals(ServiceOperationStatus.INVALID_DATA)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid data.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("STATUS: "+ status);
            }
        } else {
            logger.warn("Access denied for {}", user);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MODERATOR', 'ROLE_ADMIN')")
    @DeleteMapping("delete")
    public ResponseEntity<?> delete(Authentication authentication, @RequestParam("number") Long number) {
        Optional<User> optUser = usersService.getUserFromAuthentication(authentication);
        if (optUser.isEmpty()) {
            return accessDeniedForCompromisedUser(authentication);
        }
        User user = optUser.get();
        if (usersService.hasModeratorOrAdminRole(user)) {
            Optional<Submission> optSubmission = submissionService.findByNumber(number);
            if (optSubmission.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Submission not found.");
            Submission submission = optSubmission.get();

            ServiceOperationStatus fileDeletingStatus = fileAccessService.deleteAll(user, submission);

            if (fileDeletingStatus == ServiceOperationStatus.NOT_ALLOWED_OPERATION) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to delete these files.");
            }

            if (fileDeletingStatus == ServiceOperationStatus.MULTI_STATUS_OPERATION) {
                logger.warn("Some files were not deleted for submission: {}", submission);
                return ResponseEntity.status(HttpStatus.MULTI_STATUS).body("Some files could not be deleted.");
            }

            if (fileDeletingStatus != ServiceOperationStatus.SUCCESSFUL) {
                logger.error("Unexpected error during file deletion for submission: {}", submission);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("STATUS: " + fileDeletingStatus);
            }

            ServiceOperationStatus submissionStatus = submissionService.delete(submission);

            if (submissionStatus == ServiceOperationStatus.NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Submission not found.");
            }

            if (submissionStatus != ServiceOperationStatus.SUCCESSFUL) {
                logger.error("Failed to delete submission: {}", submission);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("STATUS: " + submissionStatus);
            }

            logger.info("Successfully deleted submission and related files: {}", submission);
            return ResponseEntity.status(HttpStatus.OK).body("Successfully deleted.");
        } else {
            logger.warn("Access denied for {}", user);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @GetMapping("getStatuses")
    public ResponseEntity<?> getAvailableStatuses(Authentication authentication) {
        Optional<User> optUser = usersService.getUserFromAuthentication(authentication);
        if (optUser.isEmpty()) {
            return accessDeniedForCompromisedUser(authentication);
        }
        User user = optUser.get();
        if (usersService.hasTeacherOrModeratorOrAdminRole(user)) {
            return ResponseEntity.status(HttpStatus.OK).body(SubmissionStatus.values());
        } else {
            logger.warn("Access denied for {}", user);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @GetMapping("getTypes")
    public ResponseEntity<?> getAvailableTypes(Authentication authentication) {
        Optional<User> optUser = usersService.getUserFromAuthentication(authentication);
        if (optUser.isEmpty()) {
            return accessDeniedForCompromisedUser(authentication);
        }
        User user = optUser.get();
        if (usersService.hasTeacherOrModeratorOrAdminRole(user)) {
            return ResponseEntity.status(HttpStatus.OK).body(SubmissionType.values());
        } else {
            logger.warn("Access denied for {}", user);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        }
    }

    private static final Map<String, String> STATUS_TRANSLATIONS = Map.of(
            "DRAFT", "Чернетка",
            "SUBMITTED", "Подано",
            "UNDER_REVIEW", "На розгляді",
            "APPROVED", "Схвалено",
            "REJECTED", "Відхилено",
            "NEEDS_REVISION", "До розгляду",
            "FINALIZED", "Завершено",
            "CANCELLED", "Скасовано"
    );

    private String translateStatus(String status) {
        return STATUS_TRANSLATIONS.getOrDefault(status, status);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @PutMapping("changeStatus")
    public ResponseEntity<?> changeStatus(Authentication authentication,
              @RequestParam("number") Long number,
              @RequestParam("newStatus") String newStatus) throws IOException, MessagingException {
        Optional<User> optUser = usersService.getUserFromAuthentication(authentication);
        if (optUser.isEmpty()) {
            return accessDeniedForCompromisedUser(authentication);
        }
        User user = optUser.get();

        if (usersService.hasTeacherOrModeratorOrAdminRole(user)) {
            ServiceOperationStatus status = submissionService.changeStatusByNumber(number, SubmissionStatus.valueOf(newStatus));
            if (status.equals(ServiceOperationStatus.SUCCESSFUL)) {
                logger.info("Submission status updated: (number: {}; newStatus: {};)", number, newStatus);
                InputStream stream = resourceLoaderService.getInputStreamFromResourceFile(TEMPLATE_LETTER_FILENAME);
                String template = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

                Optional<Submission> optionalSubmission = submissionService.findByNumber(number);
                if (optionalSubmission.isPresent()) {
                    StringBuilder letterText = new StringBuilder("");
                    letterText.append("<p>Вітаємо!</p>");
                    letterText.append("<p>Оновлено статус вашої заявки №" + number + ".</p>");
                    letterText.append("<p>Новий статус: <strong>" + translateStatus(newStatus) + "</strong>.</p>");
                    letterText.append("<p>Якщо у вас виникли запитання — зверніться до вашого деканату.</p>");
                    letterText.append("<p>З повагою,<br>Команда студентського самоврядування факультету ІІТ</p>");
                    String finalHtml = template.replace("AREA_FOR_PLACING_TEXT", letterText.toString());
                    EmailDetails letter = new EmailDetails();
                    letter.setMsgBody(finalHtml);

                    String subject = "Змінено статус заявки №" + number;
                    emailService.sendMail(letter, optionalSubmission.get().getSubmitter().getUsername(), subject);
                }

                return ResponseEntity.status(HttpStatus.OK).body("Successfully changed.");
            } else if (status.equals(ServiceOperationStatus.NOT_FOUND)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Submission not found.");
            } else if (status.equals(ServiceOperationStatus.ALREADY_EXISTS)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("STATUS: " + status);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("STATUS: " + status);
            }
        } else {
            logger.warn("Access denied for {}", user);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        }
    }
    
    private ResponseEntity<String> accessDeniedForCompromisedUser(Authentication authentication) {
        logger.warn("Access denied for {}", ((UserDetailsImpl) authentication.getPrincipal()).toString());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Compromised user.");
    }
}