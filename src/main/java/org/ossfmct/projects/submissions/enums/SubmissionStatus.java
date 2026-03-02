package org.ossfmct.projects.submissions.enums;

/**
 * Statuses of a dormitory settlement submission:
 * <ul>
 *     <li><b>DRAFT</b> — draft, the student has started but not yet submitted the application</li>
 *     <li><b>SUBMITTED</b> — the application has been submitted for review</li>
 *     <li><b>UNDER_REVIEW</b> — the application is being reviewed by the responsible party</li>
 *     <li><b>APPROVED</b> — the application has been approved</li>
 *     <li><b>REJECTED</b> — the application has been rejected</li>
 *     <li><b>NEEDS_REVISION</b> — changes are required (e.g., missing documents)</li>
 *     <li><b>FINALIZED</b> — the application process is completed (e.g., settlement is finalized)</li>
 *     <li><b>CANCELLED</b> — the application was cancelled by the student or the system</li>
 * </ul>
 */
public enum SubmissionStatus {
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    NEEDS_REVISION,
    FINALIZED,
    CANCELLED
}
