package org.ossfmct.projects.mail;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Getter
@Configuration
@Component
@EnableConfigurationProperties
@ConfigurationProperties("mail")
public class MailConfig {

    @Value("${mail.recipient}")
    private String recipient;

    @Value("${mail.subject}")
    private String subject;

    @Value("${mail.verification.subject}")
    private String verificationSubject;

    @Value("${mail.reset-password.subject}")
    private String resetPasswordSubject;

}