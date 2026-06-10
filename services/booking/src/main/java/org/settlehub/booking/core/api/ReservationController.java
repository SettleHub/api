package org.settlehub.booking.core.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.settlehub.booking.core.domain.Reservation;
import org.settlehub.booking.core.domain.Room;
import org.settlehub.booking.core.domain.RoomBooking;
import org.settlehub.booking.core.dto.BookRoomRequest;
import org.settlehub.booking.core.logic.ReservationLogic;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for managing hotel reservations.
 */
@Slf4j
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationLogic reservationLogic;

    /**
     * GET /reservations/availability?categoryId=1&checkIn=2026-06-10&checkOut=2026-06-15
     */
    @GetMapping("/availability")
    public ResponseEntity<List<Room>> checkAvailability(
            @RequestParam Long categoryId,
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut) {
        
        log.info("Received request to check availability for category {} from {} to {}", categoryId, checkIn, checkOut);
        
        List<Room> availableRooms = reservationLogic.checkAvailability(categoryId, checkIn, checkOut);
        return ResponseEntity.ok(availableRooms);
    }

    /**
     * POST /reservations/book
     */
    @PostMapping("/book")
    public ResponseEntity<?> bookRoom(@RequestBody BookRoomRequest request) {
        log.info("Received booking request from user {}", request.getUserId());
        
        try {
            Reservation reservation = reservationLogic.bookRoom(
                request.getUserId(),
                request.getCategoryId(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                request.getGuestRequest()
            );

            return ResponseEntity.ok(reservation);
        
        } catch (RuntimeException e) {
            log.warn("Booking failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /reservations/calendar?startDate=2026-06-01&endDate=2026-06-30
     * Returns a list of overlapping bookings for the frontend timeline grid.
     */
    @GetMapping("/calendar")
    public ResponseEntity<List<RoomBooking>> getCalendarData(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate) {

        return ResponseEntity.ok(reservationLogic.getBookingsForCalendar(startDate, endDate));
    }

}
