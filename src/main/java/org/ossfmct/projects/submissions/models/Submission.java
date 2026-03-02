package org.ossfmct.projects.submissions.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ossfmct.projects.files.models.UserFile;
import org.ossfmct.projects.spreadsheets.enums.Gender;
import org.ossfmct.projects.submissions.enums.SubmissionStatus;
import org.ossfmct.projects.submissions.enums.SubmissionType;
import org.ossfmct.projects.tools.Convertors;
import org.ossfmct.projects.tools.DateTool;
import org.ossfmct.projects.users.models.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.*;

/**
 * Represents a submission form related to student housing requests such as
 * settlement, relocation, eviction, etc.
 * This class holds all necessary metadata about a submission, including contact
 * information, status tracking,
 * user-submitted documents, and timestamps.
 *
 * <p>
 * Each submission is uniquely identified by a {@code number} inherited from the
 * {@link Statement} class.
 * </p>
 */
@Entity
@Getter
@Setter
@Table(name = "submissions")
@NoArgsConstructor
public final class Submission extends Statement {

    /**
     * Unique identifier (statement number).
     */
    @Id
    @Column(name = "submission_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long number;

    /**
     * The user who submitted the application.
     */
    @JsonProperty("submitter")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitter_id", referencedColumnName = "user_id")
    private User submitter;

    /**
     * The timestamp when the submission was created.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @JsonProperty("creationDate")
    @NotNull
    @Column(name = "creationDate")
    private String creationDate;

    /**
     * The current status of the submission.
     * Initialized to {@code DRAFT} by default.
     */
    @JsonProperty("status")
    @NotNull
    @Column(name = "status")
    private SubmissionStatus status = SubmissionStatus.DRAFT;

    @Convert(converter = Convertors.StatusHistoryConverter.class)
    @Column(name = "status_history", columnDefinition = "TEXT")
    private HashMap<String, SubmissionStatus> statusHistory = new HashMap<>();

    @Convert(converter = Convertors.MapToJsonConverter.class)
    @Column(name = "phones", columnDefinition = "TEXT")
    private HashMap<String, String> phones = new HashMap<>();

    @Convert(converter = Convertors.MapToJsonConverter.class)
    @Column(name = "emails", columnDefinition = "TEXT")
    private HashMap<String, String> emails = new HashMap<>();

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private SubmissionType type = SubmissionType.SETTLEMENT_STUDENT_FOR_A_YEAR;

    @Column(name = "hostel")
    private Long hostel = 0L;

    @Column(name = "floor")
    private Long floor = 0L;

    @Column(name = "section")
    private Long section = 0L;

    @Column(name = "block")
    private String block = "";

    @Column(name = "blockGender")
    private Gender blockGender = Gender.NOT_SPECIFIED;

    @Column(name = "room")
    private String room = "";

    @Column(name = "submitter_documents")
    @OneToMany(mappedBy = "linkedSubmission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<UserFile> submitterDocuments = new ArrayList<>();

    @Column(name = "description", columnDefinition = "TEXT")
    private String description = "";

    /**
     * Adds a new document to the {@code submitterDocuments} list.
     *
     * @param fileMetData object of {@link UserFile} which contains file metadata and location.
     */
    public void addSubmitterDocument(UserFile fileMetData) {
        submitterDocuments.add(fileMetData);
    }

    /**
     * Method used to change the current status and him to the history;
     * @param newStatus is updated status from user/administrators.
     */
    public void changeStatus(SubmissionStatus newStatus) {
        this.status = newStatus;
        this.statusHistory.put(DateTool.getCurrentDate(), newStatus);
    }

    public Submission(User submitter) {
        this.submitter = submitter;
    }

    public Submission(
        User submitter,
        SubmissionStatus status,
        HashMap<String, SubmissionStatus> statusHistory,
        HashMap<String, String> phones,
        HashMap<String, String> emails,
        SubmissionType type,
        List<UserFile> submitterDocuments,
        String description) {

        this.submitter = submitter != null ? submitter : new User();
        this.status = status != null ? status : SubmissionStatus.DRAFT;
        this.statusHistory = statusHistory != null ? statusHistory : new HashMap<>();
        this.phones = phones != null ? phones : new HashMap<>();
        this.emails = emails != null ? emails : new HashMap<>();
        this.type = type != null ? type : SubmissionType.SETTLEMENT_STUDENT_FOR_A_YEAR;
        this.submitterDocuments = submitterDocuments != null ? submitterDocuments : new ArrayList<>();
        this.description = description != null ? description : "";
    }

    public Submission(
            User submitter,
            HashMap<String, String> phones,
            HashMap<String, String> emails,
            SubmissionType type,
            String description) {

        this.submitter = submitter != null ? submitter : new User();
        this.statusHistory = new HashMap<>();
        this.phones = phones != null ? phones : new HashMap<>();
        this.emails = emails != null ? emails : new HashMap<>();
        this.type = type != null ? type : SubmissionType.SETTLEMENT_STUDENT_FOR_A_YEAR;
        this.submitterDocuments = new ArrayList<>();
        this.description = description != null ? description : "";
    }

    public Submission(
            User submitter,
            HashMap<String, String> phones,
            HashMap<String, String> emails,
            SubmissionType type,
            String description,
            Long hostel,
            Long floor,
            Long section,
            String block,
            Gender blockGender,
            String room) {

        this.submitter = submitter != null ? submitter : new User();
        this.statusHistory = new HashMap<>();
        this.phones = phones != null ? phones : new HashMap<>();
        this.emails = emails != null ? emails : new HashMap<>();
        this.type = type != null ? type : SubmissionType.SETTLEMENT_STUDENT_FOR_A_YEAR;
        this.submitterDocuments = new ArrayList<>();
        this.description = description != null ? description : "";
        this.hostel = hostel != null ? hostel : 0L;
        this.floor = floor != null ? floor : 0L;
        this.section = section != null ? section : 0L;
        this.block = block != null ? block : "";
        this.blockGender = blockGender != null ? blockGender : Gender.NOT_SPECIFIED;
        this.room = room != null ? room : "";
    }

    /**
     * Generates a hash code based on the {@code number}, {@code submitter}, and
     * {@code creationDate}.
     *
     * @return hash code of the submission
     */
    @Override
    public int hashCode() {
        return Objects.hash(number, submitter, creationDate);
    }

    /**
     * Checks if this submission is equal to another object.
     * Two submissions are considered equal if all their fields match.
     *
     * @param object the object to compare with
     * @return {@code true} if objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (!(object instanceof Submission))
            return false;
        Submission other = (Submission) object;
        if (this.hashCode() == other.hashCode())
            return true;
        return Objects.equals(this.number, other.number) &&
            Objects.equals(this.submitter, other.submitter) &&
            Objects.equals(this.creationDate, other.creationDate) &&
            Objects.equals(this.status, other.status) &&
            Objects.equals(this.statusHistory, other.statusHistory) &&
            Objects.equals(this.phones, other.phones) &&
            Objects.equals(this.emails, other.emails) &&
            Objects.equals(this.type, other.type) &&
            Objects.equals(this.submitterDocuments, other.submitterDocuments);
    }

    /**
     * Returns a string representation of the submission,
     * including all relevant fields and their values.
     *
     * @return a string summary of the submission object
     */
    @Override
    public String toString() {
        return "Submission{" +
            "number=" + number +
            ", submitter='" + submitter.toString() + "\'" +
            ", creationDate='" + creationDate.toString() + '\'' +
            ", status='" + status.toString() + '\'' +
            ", statusHistory=" + (statusHistory.isEmpty() ? "{}" : statusHistory.toString()) +
            ", phones=" + (phones.isEmpty() ? "{}" : phones.toString()) +
            ", emails=" + (emails.isEmpty() ? "{}" : emails.toString()) +
            ", type='" + type.toString() + '\'' +
            ", submitterDocuments=" + (submitterDocuments.isEmpty() ? "[]" : submitterDocuments.toString()) +
            ", description='" + description + '\'' +
            '}';
    }
}
