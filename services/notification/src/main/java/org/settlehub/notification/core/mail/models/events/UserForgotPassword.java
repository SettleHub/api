package org.settlehub.notification.core.mail.models.events;

public record UserForgotPassword(
    String email, 
    String code
) {}
