package org.settlehub.notification.core.mail.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Represents the payload of an email to be sent.
 * Contains the message body and any file attachments.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailDetails implements Serializable {

    /**
     * The text or HTML content of the email.
     * Initialized to an empty string to prevent NullPointerExceptions.
     */
    private String body = "";

    /**
     * Files to be attached to the email.
     * Guaranteed to be a non-null list.
     */
    private List<MultipartFile> attachments = new ArrayList<>();

    /**
     * Validates the payload before processing.
     * * @return true if the email has at least a body text, false otherwise.
     */
    public boolean isValid() {
        return !body.isEmpty();
    }

}