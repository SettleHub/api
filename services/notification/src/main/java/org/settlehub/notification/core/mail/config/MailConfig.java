package org.settlehub.notification.core.mail.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class MailConfig {

    @Value("${mail.letters.verification.subject:Verify Your Email}")
    private String verificationSubject;

    @Value("${mail.letters.reset-password.subject:Reset Your Password}")
    private String resetPasswordSubject;

    @Value("${mail.letters.registrations.subject:Welcome to our Service!}")
    private String registrationsSubject;
}