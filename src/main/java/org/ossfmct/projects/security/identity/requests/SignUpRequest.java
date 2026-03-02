package org.ossfmct.projects.security.identity.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Simple registration request template with using username, email, password and user roles .
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {
    /**
     * This value must contain user email address.
     */
    private String username;

    /**
     * Value contain user email address.
     */
    private String email;

    /**
     * Simply user password for authorization.
     */
    private String password;

    /**
     * User roles for additional access.
     */
    private Set<String> role;
}
