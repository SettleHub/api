package org.ossfmct.projects.users.models;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ossfmct.projects.security.models.Role;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity 
@Getter
@Setter
@Table(name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
    }
)
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @Column(name="user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name="username", unique = true)
    private String username;

    @Column(name="first_name")
    private String firstName = "";

    @Column(name="middle_name")
    private String middleName = "";

    @Column(name="last_name")
    private String lastName = "";

    @Column(name="course")
    private String course = "";

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name="birth_date")
    private String birthDate = "";

    @Column(name="photo_url")
    private String photoUrl = "";

    @Column(name="avatar_url")
    private String avatarUrl = "";

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name="first_sign_date")
    private String firstSignDate = "";

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name="last_sign_date")
    private String lastSignDate = "";

    @Column(name="is_verified")
    private Boolean isVerified = false;

    @JsonIgnore
    @Column(name="verification_code", unique = true)
    private String verificationCode = "";

    @JsonIgnore
    @Column(name="reset_password_code", unique = true)
    private String resetPasswordCode = "";

    @NotBlank
    @Column(name="email", unique = true)
    private String email;

    @Column(name="phone", unique = true)
    private String phone;

    @NotBlank
    @JsonIgnore
    @Column(name="password")
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public User(String username, String email, String password) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{"
                + "id=" + id
                + ", username=" + (username != null && !username.isEmpty() ? username : "''")
                + ", firstName=" + (firstName != null && !firstName.isEmpty() ? firstName : "''")
                + ", middleName=" + (middleName != null && !middleName.isEmpty() ? middleName : "''")
                + ", lastName=" + (lastName != null && !lastName.isEmpty() ? lastName : "''")
                + ", course=" + (course != null && !course.isEmpty() ? course : "''")
                + ", birthDate=" + (birthDate != null ? birthDate.toString() : "''")
                + ", photoUrl=" + (photoUrl != null && !photoUrl.isEmpty() ? photoUrl : "''")
                + ", avatarUrl=" + (avatarUrl != null && !avatarUrl.isEmpty() ? avatarUrl : "''")
                + ", firstSignDate=" + (firstSignDate != null ? firstSignDate.toString() : "''")
                + ", lastSignDate=" + (lastSignDate != null ? lastSignDate.toString() : "''")
                + ", email=" + (email != null && !email.isEmpty() ? email : "''")
                + ", phone=" + (phone != null && !phone.isEmpty() ? phone : "''")
                + ", password=" + (password != null && !password.isEmpty() ? password : "''")
                + ", roles=" + (roles != null && !roles.isEmpty() ? roles : "''")
                + "}";
    }
}