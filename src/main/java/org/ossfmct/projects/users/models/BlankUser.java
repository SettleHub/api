package org.ossfmct.projects.users.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ossfmct.projects.security.models.Role;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "blank_users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
    }
)
@AllArgsConstructor
@NoArgsConstructor
public class BlankUser {
    @Id
    @Column(name="blank_user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="first_name")
    private String firstName = "";

    @Column(name="middle_name")
    private String middleName = "";

    @Column(name="last_name")
    private String lastName = "";

    @Column(name="course")
    private String course = "";

    @NotBlank
    @Column(name="email", unique = true)
    private String email;

    @Column(name="phone", unique = true)
    private String phone;

    @Column(name="is_already_registered")
    private Boolean isAlreadyRegistered = false;

    @Override
    public String toString() {
        return "BlankUser{"
                + "id=" + id
                + ", firstName='" + (firstName != null && !firstName.isEmpty() ? firstName : "") + "'"
                + ", middleName='" + (middleName != null && !middleName.isEmpty() ? middleName : "") + "'"
                + ", lastName='" + (lastName != null && !lastName.isEmpty() ? lastName : "") + "'"
                + ", course='" + (email != null && !email.isEmpty() ? email : "") + "'"
                + ", email='" + (email != null && !email.isEmpty() ? email : "") + "'"
                + ", phone='" + (phone != null && !phone.isEmpty() ? phone : "") + "'"
                + ", isAlreadyRegistered=" + (isAlreadyRegistered != null ? isAlreadyRegistered : false)
                + "}";
    }
}
