package org.ossfmct.projects.submissions.enums;

/**
 * Types of submissions related to dormitory housing processes:
 * <ul>
 *     <li><b>SETTLEMENT_STUDENT_FOR_A_YEAR</b> — settlement of a current student for a year</li>
 *     <li><b>SETTLEMENT_STUDENT_FOR_A_SUMMER</b> — settlement of a current student for a summer season</li>
 *     <li><b>SETTLEMENT_APPLICANT_FOR_A_YEAR</b> — settlement of a university applicant for a year</li>
 *     <li><b>SETTLEMENT_APPLICANT_FOR_A_SUMMER</b> — settlement of a university applicant for a summer season</li>
 *     <li><b>RELOCATION</b> — relocation to another room or dormitory</li>
 *     <li><b>EVICTION</b> — eviction (either voluntary or mandatory)</li>
 *     <li><b>EXTENSION</b> — extension of the accommodation period</li>
 *     <li><b>CANCELLATION</b> — cancellation of a previously submitted request</li>
 * </ul>
 */
public enum SubmissionType {
    SETTLEMENT_STUDENT_FOR_A_YEAR,
    SETTLEMENT_STUDENT_FOR_A_SUMMER,
    SETTLEMENT_APPLICANT_FOR_A_YEAR,
    SETTLEMENT_APPLICANT_FOR_A_SUMMER,
    RELOCATION,
    EVICTION,
    EXTENSION,
    CANCELLATION
}
