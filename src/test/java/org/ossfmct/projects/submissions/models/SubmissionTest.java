package org.ossfmct.projects.submissions.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ossfmct.projects.submissions.enums.SubmissionStatus;
import org.ossfmct.projects.submissions.enums.SubmissionType;
import org.ossfmct.projects.users.models.User;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SubmissionTest {

    private Submission submission1;
    private Submission submission2;

    @BeforeEach
    void setUp() {
        User user = new User(); // Create a mock user
        submission1 = new Submission();
        submission2 = new Submission();

        submission1.setStatus(SubmissionStatus.DRAFT);
        submission2.setStatus(SubmissionStatus.DRAFT);

        submission1.setStatusHistory(new HashMap<>());
        submission2.setStatusHistory(new HashMap<>());

        submission1.setPhones(new HashMap<>());
        submission2.setPhones(new HashMap<>());

        submission1.setEmails(new HashMap<>());
        submission2.setEmails(new HashMap<>());

        submission1.setType(SubmissionType.SETTLEMENT_STUDENT_FOR_A_YEAR);
        submission2.setType(SubmissionType.SETTLEMENT_STUDENT_FOR_A_YEAR);

        submission1.setDescription("Test description");
        submission2.setDescription("Test description");
    }

    @Test
    void testEquals_Failure() {
        assertFalse(submission1.equals(submission2), "Different submissions should not be equal.");
    }

    @Test
    void testEquals_NullObject() {
        assertFalse(submission1.equals(null), "Submission should not be equal to null.");
    }

    @Test
    void testHashCode_Failure() {
        assertNotEquals(submission1.hashCode(), submission2.hashCode(), "Different submissions with the same fields should have the different hash codes.");
    }

    @Test
    void testHashCode_Success() {
        submission2.setDescription("Another description");
        assertEquals(submission1.hashCode(), submission1.hashCode(), "One submission must have a same hash code every time.");
    }

    @Test
    void testToString() {
        String expectedString = "Submission{" +
                "number='" + submission1.getNumber() + '\'' +
                ", submitter=" + submission1.getSubmitter().toString() +
                ", creationDate='" + submission1.getCreationDate().toString() + '\'' +
                ", status='" + submission1.getStatus().toString() + '\'' +
                ", statusHistory={}"+
                ", phones={}"+
                ", emails={}"+
                ", type='SETTLEMENT_STUDENT'" +
                ", submitterDocuments=[doc1, doc2]" +
                ", description='Test description'" +
                '}';

        assertEquals(expectedString, submission1.toString(), "toString() should return a string representation of the Submission.");
    }

    @Test
    void testSubmissionType() {
        assertEquals(SubmissionType.SETTLEMENT_STUDENT_FOR_A_YEAR, submission1.getType(), "The submission type should be SETTLEMENT_STUDENT by default.");
    }

    @Test
    void testStatusHistory() {
        submission1.getStatusHistory().put(Date.from(Instant.now()).toString(), SubmissionStatus.APPROVED);
        assertTrue(submission1.getStatusHistory().containsValue(SubmissionStatus.APPROVED), "Status history should contain the new status.");
    }

    @Test
    void testPhones() {
        submission1.getPhones().put("123456789", "John Doe");
        assertTrue(submission1.getPhones().containsKey("123456789"), "Phones map should contain the phone number.");
    }

    @Test
    void testEmails() {
        submission1.getEmails().put("email@example.com", "Jane Doe");
        assertTrue(submission1.getEmails().containsKey("email@example.com"), "Emails map should contain the email address.");
    }

    @Test
    void testDescription() {
        submission1.setDescription("Updated description");
        assertEquals("Updated description", submission1.getDescription(), "Description should be set correctly.");
    }
}
