package org.settlehub.notification.core.mail.db;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "mail_history")
public class MailHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String recipient;
    
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MailStatus status;

    @Column(length = 1000)
    private String errorReason;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
    
}
