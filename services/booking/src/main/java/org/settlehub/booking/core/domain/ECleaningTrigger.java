package org.settlehub.booking.core.domain;

/**
 * Types of events that trigger a room to become DIRTY.
 */
public enum ECleaningTrigger {
    ON_CHECKOUT,         // Cleaning after a guest's departure
    DAILY_OCCUPIED,      // Daily housekeeping (refreshing the room while the guest is staying)
    PRE_CHECKIN          // Pre-arrival cleaning of an empty room
}
