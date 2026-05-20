package org.settlehub.notification.core.mail.kafka;

import org.settlehub.notification.core.mail.config.MailConfig;
import org.settlehub.notification.core.mail.interfaces.IMailService;
import org.settlehub.notification.core.mail.models.MailDetails;
import org.settlehub.notification.core.mail.models.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MailEventListener {

    private static final Logger logger = LoggerFactory.getLogger(MailEventListener.class);
    @Autowired private IMailService mailService;
    @Autowired private MailConfig mailConfig;

    @KafkaListener(topics = "user-registrations", groupId = "notification-group")
    public void handleUserRegistration(UserRegisteredEvent event) {
        try {
            logger.info("Received registration event for user: {}", event.username());
            
            MailDetails details = new MailDetails();
            details.setBody("<h1>Вітаємо, " + event.username() + "!</h1><p>Ви успішно зареєструвалися в системі SettleHub.</p>");
            
            mailService.sendMail(details, event.email(), mailConfig.getRegistrationsSubject());
            
        } catch (Exception e) {
            logger.error("Failed to send registration email to {}: {}", event.email(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "user-forgot-password", groupId = "notification-group")
    public void handleForgotPassword(UserForgotPassword event) {
        try {
            logger.info("Received password reset request for: {}", event.email());
            
            MailDetails details = new MailDetails();
            details.setBody("<h1>Скидання пароля</h1><p>Ваш код для скидання пароля: <b>" + event.code() + "</b></p>");
            
            mailService.sendMail(details, event.email(), mailConfig.getResetPasswordSubject());
            
        } catch (Exception e) {
            logger.error("Failed to send password reset code to {}: {}", event.email(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "user-email-verified", groupId = "notification-group")
    public void handleEmailVerified(UserEmailVerified event) {
        try {
            logger.info("Received email verification event for: {}", event.email());
            
            MailDetails details = new MailDetails();
            details.setBody("<h1>Email підтверджено!</h1><p>Ваша поштова скринька успішно верифікована. Дякуємо!</p>");
            
            mailService.sendMail(details, event.email(), mailConfig.getVerificationSubject());
            
        } catch (Exception e) {
            logger.error("Failed to send verification confirmation email to {}: {}", event.email(), e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
}

