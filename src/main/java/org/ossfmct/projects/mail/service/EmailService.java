package org.ossfmct.projects.mail.service;

import org.ossfmct.projects.mail.MailConfig;
import org.ossfmct.projects.mail.enums.MailStatus;
import org.ossfmct.projects.mail.interfaces.IEmailService;
import org.ossfmct.projects.mail.models.EmailDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class EmailService implements IEmailService {

    @Autowired private JavaMailSender javaMailSender;

    @Autowired private MailConfig config;

    @Value("${spring.mail.username}")
    private String sender;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Override
    public MailStatus sendMail(EmailDetails details, String recipient, String subject) throws MailException, MessagingException, IOException {
        MailStatus status = sendMailLetter(details, recipient, subject);
        if (status.equals(MailStatus.SENT)) logger.info("sent mail to: {}", recipient);
        return status;
    }

    @Override
    public MailStatus sendVerificationMail(EmailDetails details, String recipient) throws MailException, MessagingException, IOException {
        MailStatus status = sendMailLetter(details, recipient, config.getVerificationSubject());
        if (status.equals(MailStatus.SENT)) logger.info("sent verification mail to: {}", recipient);
        return status;
    }

    @Override
    public MailStatus sendResetPasswordMail(EmailDetails details, String recipient) throws MailException, MessagingException, IOException {
        MailStatus status = sendMailLetter(details, recipient, config.getResetPasswordSubject());
        if (status.equals(MailStatus.SENT)) logger.info("sent reset password code to: {}", recipient);
        return status;
    }

    private MailStatus sendMailLetter(EmailDetails details, String recipient, String subject) throws MailException, MessagingException, IOException {
        if (!details.isValid()) {
            return MailStatus.ERROR;
        }

        MimeMessage letter = javaMailSender.createMimeMessage();
        MimeMessageHelper letterHelper = new MimeMessageHelper(letter, true);

        letterHelper.setFrom(sender);
        letterHelper.setTo(recipient);
        letterHelper.setText(details.getMsgBody(), true);
        letterHelper.setSubject(subject);
        for (MultipartFile file : details.getAttachments()) {
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : file.getName();
            letterHelper.addAttachment(filename, file);
        }

        javaMailSender.send(letter);
        return MailStatus.SENT;
    }
}