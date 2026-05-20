package org.settlehub.notification.core.mail.services;

import org.settlehub.notification.core.mail.db.MailHistory;
import org.settlehub.notification.core.mail.db.MailHistoryRepository;
import org.settlehub.notification.core.mail.db.MailStatus;
import org.settlehub.notification.core.mail.interfaces.IMailService;
import org.settlehub.notification.core.mail.models.MailDetails;
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
public class EmailService implements IMailService {

    @Autowired private JavaMailSender javaMailSender;
    @Autowired private MailHistoryRepository mailHistoryRepository;

    @Value("${spring.mail.username}")
    private String sender;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Override
    public void sendMail(MailDetails details, String recipient, String subject) throws MailException, MessagingException, IOException {
        sendMailLetter(details, recipient, subject);
        logger.info("Sent mail to: {}", recipient);
    }

    private void sendMailLetter(MailDetails details, String recipient, String subject) throws MailException, MessagingException, IOException {
        
        MailHistory history = new MailHistory();
        history.setRecipient(recipient);
        history.setSubject(subject);
        
        if (!details.isValid()) {
            logger.warn("Mail details are invalid (empty body) for recipient: {}", recipient);
            history.setStatus(MailStatus.INVALID);
            history.setErrorReason("The body of the message is empty or the details are invalid.");
            mailHistoryRepository.save(history);
            return;
        }

        MimeMessage letter = javaMailSender.createMimeMessage();
        MimeMessageHelper letterHelper = new MimeMessageHelper(letter, true);

        letterHelper.setFrom(sender);
        letterHelper.setTo(recipient);
        letterHelper.setText(details.getBody(), true);
        letterHelper.setSubject(subject);
        
        if (details.getAttachments() != null) {
            for (MultipartFile file : details.getAttachments()) {
                String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : file.getName();
                letterHelper.addAttachment(filename, file);
            }
        }

        try {
            javaMailSender.send(letter);
            history.setStatus(MailStatus.SUCCESS);
            mailHistoryRepository.save(history);
        } catch (Exception e) {
            history.setStatus(MailStatus.FAILED);
            history.setErrorReason(e.getMessage());
            mailHistoryRepository.save(history);
            throw e;
        }

    }
}
