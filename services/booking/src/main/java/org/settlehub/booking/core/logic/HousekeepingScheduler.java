package org.settlehub.booking.core.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.settlehub.booking.core.domain.*;
import org.settlehub.booking.core.repo.HousekeepingRuleRepo;
import org.settlehub.booking.core.repo.RoomBookingRepo;
import org.settlehub.booking.core.repo.RoomRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dynamic Rule-Engine Scheduler for Housekeeping.
 * Evaluates active rules stored in the database and triggers cleaning tasks accordingly.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HousekeepingScheduler {

    private final RoomBookingRepo roomBookingRepo;
    private final RoomRepo roomRepo;
    private final HousekeepingRuleRepo ruleRepo;

    /**
     * Wakes up at the start of every hour to evaluate and execute active housekeeping rules.
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void executeDynamicRules() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        DayOfWeek currentDayOfWeek = today.getDayOfWeek();
        int currentHour = now.getHour();

        log.info("Waking up rule engine at hour: {}. Day of week: {}", currentHour, currentDayOfWeek);

        List<HousekeepingRule> allActiveRules = ruleRepo.findAll().stream()
                .filter(HousekeepingRule::getIsActive)
                .collect(Collectors.toList());

        for (HousekeepingRule rule : allActiveRules) {
            
            if (rule.getExecutionTime().getHour() != currentHour) {
                continue;
            }

            if (rule.getDayOfWeek() != null && rule.getDayOfWeek() != currentDayOfWeek) {
                continue;
            }

            log.info("Executing Rule: [{}] (Type: {})", rule.getName(), rule.getTriggerType());
            applyRule(rule, today);
        }
    }

    /**
     * Applies the specific logic of a housekeeping rule based on its trigger type.
     *
     * @param rule  the housekeeping rule to apply
     * @param today the current date
     */
    private void applyRule(HousekeepingRule rule, LocalDate today) {
        switch (rule.getTriggerType()) {
            case ON_CHECKOUT:
                List<RoomBooking> checkouts = roomBookingRepo.findByCheckOutDate(today);
                for (RoomBooking booking : checkouts) {
                    Room room = booking.getRoom();
                    if (isCategoryMismatch(rule, room)) {
                        continue;
                    }
                    markDirtyIfNotAlready(room);
                }
                break;

            case DAILY_OCCUPIED:
                List<Room> occupiedRooms = roomRepo.findByStatus(ERoomStatus.OCCUPIED);
                for (Room room : occupiedRooms) {
                    if (isCategoryMismatch(rule, room)) {
                        continue;
                    }
                    markDirtyIfNotAlready(room);
                }
                break;

            case PRE_CHECKIN:
                List<RoomBooking> checkins = roomBookingRepo.findByCheckInDate(today);
                for (RoomBooking booking : checkins) {
                    Room room = booking.getRoom();
                    if (room.getStatus() == ERoomStatus.AVAILABLE) {
                        if (isCategoryMismatch(rule, room)) {
                            continue;
                        }
                        markDirtyIfNotAlready(room);
                    }
                }
                break;
        }
    }

    /**
     * Checks if the rule is targeted at a specific category and if the room matches it.
     *
     * @param rule the housekeeping rule
     * @param room the room to check
     * @return true if the rule specifies a category and the room does not match it, false otherwise
     */
    private boolean isCategoryMismatch(HousekeepingRule rule, Room room) {
        return rule.getTargetCategory() != null 
                && !room.getCategory().getId().equals(rule.getTargetCategory().getId());
    }

    /**
     * Updates the room's status to DIRTY if it is not already DIRTY or OUT_OF_ORDER.
     *
     * @param room the room to update
     */
    private void markDirtyIfNotAlready(Room room) {
        if (room.getStatus() != ERoomStatus.DIRTY && room.getStatus() != ERoomStatus.OUT_OF_ORDER) {
            room.setStatus(ERoomStatus.DIRTY);
            roomRepo.save(room);
            log.info("Room {} dynamically marked as DIRTY.", room.getNumber());
        }
    }
}
