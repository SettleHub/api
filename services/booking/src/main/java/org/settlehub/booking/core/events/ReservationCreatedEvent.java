package org.settlehub.booking.core.events;

import java.time.Instant;

/**
 * Event published when a new reservation is successfully created.
 */
public record ReservationCreatedEvent(
    Long reservationId,
    Long userId,
    String status,
    Instant timestamp
) {
    public ReservationCreatedEvent(Long reservationId, Long userId, String status) {
        this(reservationId, userId, status, Instant.now());
    }
}
