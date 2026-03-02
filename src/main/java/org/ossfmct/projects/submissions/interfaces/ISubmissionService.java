package org.ossfmct.projects.submissions.interfaces;

import org.javatuples.Pair;
import org.ossfmct.projects.submissions.enums.ServiceOperationStatus;
import org.ossfmct.projects.submissions.models.Submission;
import org.ossfmct.projects.users.models.User;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@link Submission} entities.
 * <p>
 * Extends {@link IStatementService} to include functionality for generating
 * unique statement numbers.
 * Provides methods for adding, updating, deleting, and retrieving submissions.
 * </p>
 */
public interface ISubmissionService extends IStatementService {

    List<Submission> getAll();

    /**
     * Adds a new submission to the system.
     *
     * @param submission the submission object to be added
     * @return true if success;
     */
    ServiceOperationStatus add(Submission submission);

    /**
     * Updates an existing submission with new values.
     *
     * @param existing the current submission to be updated
     * @param updated  the submission containing updated values
     * @return true if success;
     */
    ServiceOperationStatus update(Submission existing, Submission updated);

    /**
     * Updates an existing submission identified by its unique number.
     *
     * @param number  the unique number of the submission to update
     * @param updated the submission object containing the new values
     * @return true if success;
     */
    ServiceOperationStatus updateByNumber(Long number, Submission updated);

    /**
     * Deletes a submission from the system.
     *
     * @param submission the submission object to delete
     * @return true if success;
     */
    ServiceOperationStatus delete(Submission submission);

    /**
     * Deletes a submission identified by its unique number.
     *
     * @param number the unique number of the submission to delete
     * @return true if success;
     */
    ServiceOperationStatus deleteByNumber(Long number);

    /**
     * Finds a submission by its unique number.
     *
     * @param number the unique number of the submission
     * @return an {@link Optional} containing the found submission, or empty if not
     *         found
     */
    Optional<Submission> findByNumber(Long number);

    /**
     * Finds all submissions created on a specific date.
     *
     * @param date the date to search for
     * @return a list of submissions created on the specified date
     */
    List<Submission> findByDate(String date);

    /**
     * Finds all submissions submitted by a specific user.
     *
     * @param submitter the user who submitted the submissions
     * @return a list of submissions submitted by the given user
     */
    List<Submission> findBySubmitter(User submitter);
}
