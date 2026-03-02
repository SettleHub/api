package org.ossfmct.projects.submissions.services;

import lombok.AllArgsConstructor;
import org.ossfmct.projects.mail.service.EmailService;
import org.ossfmct.projects.submissions.SubmissionsConfig;
import org.ossfmct.projects.submissions.enums.ServiceOperationStatus;
import org.ossfmct.projects.submissions.enums.SubmissionStatus;
import org.ossfmct.projects.submissions.interfaces.ISubmissionService;
import org.ossfmct.projects.submissions.models.Submission;
import org.ossfmct.projects.submissions.repositories.SubmissionRepository;
import org.ossfmct.projects.tools.DateTool;
import org.ossfmct.projects.users.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class SubmissionService implements ISubmissionService {

    private final static Logger logger = LoggerFactory.getLogger(SubmissionService.class);

    private final SubmissionRepository repository;

    private static Set<SubmissionStatus> activeStatuses = Set.of(
        SubmissionStatus.SUBMITTED,
        SubmissionStatus.NEEDS_REVISION,
        SubmissionStatus.UNDER_REVIEW,
        SubmissionStatus.APPROVED
    );

    @Override
    public List<Submission> getAll() {
        return repository.findAll();
    }

    /**
     * Adds a new submission to the system.
     *
     * @param submission the submission object to be added
     */
    @Override
    public ServiceOperationStatus add(Submission submission) {
        if (repository.findByNumber(submission.getNumber()).isEmpty()) {
            submission.setCreationDate(DateTool.getCurrentDate());
            repository.save(submission);
            return ServiceOperationStatus.SUCCESSFUL;
        } else {
            return ServiceOperationStatus.ALREADY_EXISTS;
        }
    }

    /**
     * Updates an existing submission with new values.
     *
     * @param target  the current submission to be updated
     * @param updated the submission containing updated values
     */
    @Override
    public ServiceOperationStatus update(Submission target, Submission updated) {
        Optional<Submission> optional = repository.findByNumber(target.getNumber());
        if (optional.isEmpty()) return ServiceOperationStatus.NOT_FOUND;
        Submission existing = optional.get();
        updated.setStatusHistory(existing.getStatusHistory());
        updated.setSubmitterDocuments(existing.getSubmitterDocuments());
        if (!(existing.getStatus().equals(updated.getStatus()))) updated.changeStatus(updated.getStatus());
        if (existing.equals(target)) {
            repository.save(updated);
            return ServiceOperationStatus.SUCCESSFUL;
        } else {
            return ServiceOperationStatus.INVALID_DATA;
        }
    }

    /**
     * Updates an existing submission identified by its unique number.
     *
     * @param number  the unique number of the submission to update
     * @param updated the submission object containing the new values
     */
    @Override
    public ServiceOperationStatus updateByNumber(Long number, Submission updated) {
        Optional<Submission> optional = repository.findByNumber(number);
        if (optional.isEmpty())
            return ServiceOperationStatus.NOT_FOUND;
        Submission existing = optional.get();
        return update(existing, updated);
    }

    /**
     * Deletes a submission from the system.
     *
     * @param submission the submission object to delete
     */
    @Override
    public ServiceOperationStatus delete(Submission submission) {
        Optional<Submission> optional = repository.findByNumber(submission.getNumber());
        if (optional.isEmpty())
            return ServiceOperationStatus.NOT_FOUND;
        Submission existing = optional.get();
        if (existing.equals(submission)) {
            repository.delete(submission);
            return ServiceOperationStatus.SUCCESSFUL;
        }
        return ServiceOperationStatus.DELETE_FAILED;
    }

    /**
     * Deletes a submission identified by its unique number.
     *
     * @param number the unique number of the submission to delete
     */
    @Override
    public ServiceOperationStatus deleteByNumber(Long number) {
        Optional<Submission> optional = repository.findByNumber(number);
        if (optional.isEmpty())
            return ServiceOperationStatus.NOT_FOUND;
        Submission existing = optional.get();
        return delete(existing);
    }

    /**
     * Finds a submission by its unique number.
     *
     * @param number the unique number of the submission
     * @return an {@link Optional} containing the found submission, or empty if not
     *         found
     */
    @Override
    public Optional<Submission> findByNumber(Long number) {
        return repository.findByNumber(number);
    }

    /**
     * Finds all submissions created on a specific date.
     *
     * @param date the date to search for
     * @return a list of submissions created on the specified date
     */
    @Override
    public List<Submission> findByDate(String date) {
        return repository.findAllByCreationDate(date);
    }

    /**
     * Finds all submissions submitted by a specific user.
     *
     * @param submitter the user who submitted the submissions
     * @return a list of submissions submitted by the given user
     */
    @Override
    public List<Submission> findBySubmitter(User submitter) {
        return repository.findAllBySubmitter(submitter);
    }

    public Long countOfActiveSubmissionsBySubmitter(User submitter) {
        return findBySubmitter(submitter).stream()
            .filter(submission -> activeStatuses.contains(submission.getStatus()))
            .count();
    }

    public ServiceOperationStatus changeStatus(Submission submission, SubmissionStatus newStatus) {
        Optional<Submission> optExisting = findByNumber(submission.getNumber());
        if (optExisting.isEmpty()) return ServiceOperationStatus.NOT_FOUND;
        Submission existing = optExisting.get();
        if (existing.getStatus().equals(newStatus)) return ServiceOperationStatus.ALREADY_EXISTS;
        existing.changeStatus(newStatus);
        repository.save(existing);
        return ServiceOperationStatus.SUCCESSFUL;
    }

    public ServiceOperationStatus changeStatusByNumber(Long number, SubmissionStatus newStatus) {
        Optional<Submission> optSubmission = findByNumber(number);
        if (optSubmission.isEmpty()) return ServiceOperationStatus.NOT_FOUND;
        Submission submission = optSubmission.get();
        return changeStatus(submission, newStatus);
    }

}
