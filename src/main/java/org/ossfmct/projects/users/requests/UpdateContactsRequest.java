package org.ossfmct.projects.users.requests;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Simple request template with using email, phone and birthDate.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateContactsRequest {
    /**
     * User email address.
     */
    private String email;

    /**
     * User phone number.
     */
    private String phone;

    /**
     * User birth date.
     */
    private String birthDate;
}