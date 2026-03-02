package org.ossfmct.projects.files.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ossfmct.projects.submissions.models.Submission;
import org.ossfmct.projects.users.models.User;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "user_files")
@NoArgsConstructor
@AllArgsConstructor
public class UserFile {

    @Id
    @Column(name="file_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("owner")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", referencedColumnName = "user_id")
    private User owner;

    @JsonProperty("filename")
    @NotNull
    @Column(name = "filename")
    private String filename;

    /**
     * storage path it is string of:
     * <ol>
     *     <li>user id</li>
     *     <li>UNIX delimiter (forward slash)</li>
     *     <li>filename with his extension</li>
     * </ol>
     */
    @JsonProperty("storagePath")
    @NotNull
    @Column(name = "storagePath")
    private String storagePath;

    @JsonProperty("mimeType")
    @Column(name = "mimeType")
    private String mimeType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @JsonProperty("createdAt")
    @NotNull
    @Column(name = "createdAt")
    private String createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_number")
    @JsonBackReference
    private Submission linkedSubmission;

    /**
     * Generates a hash code based on the all class fields
     *
     * @return hash code of the file
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, owner, filename, storagePath, mimeType, createdAt);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (!(object instanceof UserFile))
            return false;
        UserFile other = (UserFile) object;
        if (this.hashCode() == other.hashCode())
            return true;
        return Objects.equals(this.id, other.id) &&
            Objects.equals(this.owner, other.owner) &&
            Objects.equals(this.filename, other.filename) &&
            Objects.equals(this.storagePath, other.storagePath) &&
            Objects.equals(this.mimeType, other.mimeType) &&
            Objects.equals(this.createdAt, other.createdAt);
    }

    /**
     * Returns a string representation of the file,
     * including all relevant fields and their values.
     *
     * @return a string summary of the file object
     */
    @Override
    public String toString() {
        return "File{" +
            "id=" + id +
            ", owner=" + owner.toString() +
            ", filename='" + filename + '\'' +
            ", storagePath='" + storagePath + '\'' +
            ", mimeType='" + mimeType + '\'' +
            ", createdAt='" + createdAt + '\'' +
            '}';
    }
}
