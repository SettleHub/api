package org.settlehub.notification.core.mail.models.events;

import java.util.Set;

public record UserRegisteredEvent(
    String username, 
    String email, 
    Set<String> roles
) {}
