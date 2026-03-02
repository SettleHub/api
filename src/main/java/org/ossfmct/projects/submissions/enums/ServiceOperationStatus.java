package org.ossfmct.projects.submissions.enums;

/**
 * Enumeration of common errors that can occur during submission service operations.
 */
public enum ServiceOperationStatus {

    /**
     * The operation proceed successful
     */
    SUCCESSFUL,

    /**
     * The submission with the same unique number already exists.
     */
    ALREADY_EXISTS,

    /**
     * The requested submission was not found in the database.
     */
    NOT_FOUND,

    /**
     * Failed to create the submission due to database or validation error.
     */
    CREATION_FAILED,

    /**
     * Failed to update the submission due to invalid state or missing data.
     */
    UPDATE_FAILED,

    /**
     * Failed to delete the submission (may not exist or deletion is restricted).
     */
    DELETE_FAILED,

    /**
     * Submission has invalid or incomplete data.
     */
    INVALID_DATA,

    /**
     * Submission status transition is not allowed.
     */
    INVALID_STATUS_TRANSITION,

    /**
     * The operation is not supported for the current submission type or state.
     */
    UNSUPPORTED_OPERATION,

    /**
     * The operation is not allowed for the current submission type or state.
     */
    NOT_ALLOWED_OPERATION,

    /**
     * The operation has a multiple statuses for the current submission type or state.
     */
    MULTI_STATUS_OPERATION
}
