package org.ossfmct.projects.mail.interfaces;

import jakarta.mail.MessagingException;
import org.ossfmct.projects.mail.enums.MailStatus;
import org.ossfmct.projects.mail.models.EmailDetails;
import org.springframework.mail.MailException;

import java.io.IOException;

public interface IEmailService {

    /**
     * @param details {@link EmailDetails} object.
     * @return {@link MailStatus} which contain status of operation.
     * @throws IOException if something fails.
     */
    MailStatus sendMail(EmailDetails details, String recipient, String subject) throws MailException, MessagingException, IOException;

    /**
     * Sends letters which contains user verification codes.
     * Needs for user registration.
     * @param details {@link EmailDetails} object.
     * @param recipient is email of new registered user without verification.
     * @return {@link MailStatus} which contain status of operation.
     * @throws IOException if something fails.
     */
    MailStatus sendVerificationMail(EmailDetails details, String recipient) throws IOException, MessagingException;

    /**
     * Sends letters which contains user reset password code.
     * Needs for user password reset.
     * @param details {@link EmailDetails} object.
     * @param recipient is email of already registered user.
     * @return {@link MailStatus} which contain status of operation.
     * @throws IOException if something fails.
     */
    MailStatus sendResetPasswordMail(EmailDetails details, String recipient) throws IOException, MessagingException;
}