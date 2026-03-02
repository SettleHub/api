package org.ossfmct.projects.submissions.interfaces;

/**
 * Interface for a service that generates a unique number for statements.
 * <p>
 * The unique number is used to identify different types of statements (such as
 * submissions, requests, etc.).
 * This number should be generated in a way that guarantees uniqueness across
 * all instances.
 * </p>
 */
public interface IStatementService {

    /**
     * Generates a unique number for a statement.
     * <p>
     * The number returned by this method is guaranteed to be unique, and it serves
     * as an identifier for
     * the statement. The exact mechanism for generating this unique number is
     * implementation-dependent.
     * </p>
     *
     * @return a unique number as a Long that will be associated with the statement
     */
    // Long generateUniqueNumber();
}
