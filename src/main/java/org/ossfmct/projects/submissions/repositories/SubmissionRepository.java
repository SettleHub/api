package org.ossfmct.projects.submissions.repositories;

import org.ossfmct.projects.submissions.models.Submission;
import org.ossfmct.projects.users.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing {@link Submission} data.
 * Provides basic CRUD operations and custom queries by number, date, and
 * submitter.
 */
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    /**
     * Finds a submission by its unique number.
     *
     * @param number the unique identifier of the submission
     * @return an Optional containing the submission if found, or empty otherwise
     */
    Optional<Submission> findByNumber(Long number);

    /**
     * Finds all submissions created on the given date.
     *
     * @param creationDate the date when the submissions were created
     * @return a list of submissions created on the specified date
     */
    List<Submission> findAllByCreationDate(String creationDate);

    /**
     * Finds all submissions submitted by a given user.
     *
     * @param submitter the user who submitted the submissions
     * @return a list of submissions submitted by the specified user
     */
    List<Submission> findAllBySubmitter(User submitter);

    /**
     * Deletes a submission by its unique number.
     *
     * @param number the unique number of the submission to delete
     */
    void deleteByNumber(Long number);
}
