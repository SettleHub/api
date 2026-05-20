package org.settlehub.notification.core.mail.interfaces;

import org.settlehub.notification.core.mail.models.MailDetails;
import jakarta.mail.MessagingException;
import java.io.IOException;

public interface IMailService {
    void sendMail(MailDetails details, String recipient, String subject) throws MessagingException, IOException;
}
