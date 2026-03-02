package org.ossfmct.projects.submissions.models;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

/**
 * Abstract base class representing a generic application statement.
 * <p>
 * <b>Features:</b>
 * </p>
 * <ul>
 * <li><b>number</b> — a unique identifier for the submission</li>
 * </ul>
 *
 * <p>
 * This class should be extended to create specific types of statements, such as
 * dormitory settlement or eviction requests.
 * </p>
 */
@Getter
public abstract class Statement {

    /**
     * Unique identifier (statement number).
     */
    protected Long number;
}
