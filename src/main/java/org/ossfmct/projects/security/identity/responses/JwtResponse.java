package org.ossfmct.projects.security.identity.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Response to authorization client request.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    /**
     * String which contain jwt key for client Bearer authorization.
     */
    private String jwt;

    /**
     * User id from database.
     */
    private long id;

    /**
     * Username with which the authorisation was carried out.
     */
    private String username;

    /**
     * User email address (is also username).
     */
    private String email;

    /**
     * User roles in on service.
     */
    private List<String> roles;
}
