package org.ossfmct.projects.security.identity.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Simple registration request template with using email and password.
 * Assigned for students which data already exists in database.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequestCompleteWithEmail {

    /**
     * Value contain user email address.
     */
    private String email;

    /**
     * Simply user password for authorization.
     */
    private String password;

}
