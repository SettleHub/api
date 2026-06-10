package org.settlehub.booking.core.logic;

import java.time.LocalDate;
import java.util.List;

import org.settlehub.booking.core.domain.EReservationStatus;
import org.settlehub.booking.core.domain.Reservation;
import org.settlehub.booking.core.domain.Room;
import org.settlehub.booking.core.domain.RoomBooking;
import org.settlehub.booking.core.domain.RoomCategory;
import org.settlehub.booking.core.events.ReservationCreatedEvent;
import org.settlehub.booking.core.repo.ReservationRepo;
import org.settlehub.booking.core.repo.RoomBookingRepo;
import org.settlehub.booking.core.repo.RoomCategoryRepo;
import org.settlehub.booking.core.repo.RoomRepo;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Core business logic for managing hotel reservations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationLogic {

    private final RoomRepo roomRepo;
    private final RoomBookingRepo roomBookingRepo;
    private final ReservationRepo reservationRepo;
    private final RoomCategoryRepo categoryRepo;
    
    // Injecting KafkaTemplate to produce messages
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // The topic name where payment and notification services will be listening
    private static final String RESERVATION_TOPIC = "reservation-created";

    /**
     * Checks the availability of rooms for a specific category within a given date range.
     *
     * @param categoryId the ID of the room category
     * @param checkIn    the desired check-in date
     * @param checkOut   the desired check-out date
     * @return a list of available rooms matching the given criteria
     */
    public List<Room> checkAvailability(Long categoryId, LocalDate checkIn, LocalDate checkOut) {
        log.info("Checking availability for category {} from {} to {}", categoryId, checkIn, checkOut);
        return roomRepo.findAvailableRooms(categoryId, checkIn, checkOut);
    }

    /**
     * Overloaded method to check availability using a RoomCategory entity.
     *
     * @param category the room category entity
     * @param checkIn  the desired check-in date
     * @param checkOut the desired check-out date
     * @return a list of available rooms matching the given criteria
     */
    public List<Room> checkAvailability(RoomCategory category, LocalDate checkIn, LocalDate checkOut) {
        return checkAvailability(category.getId(), checkIn, checkOut);
    }

    /**
     * Creates a new reservation for a user.
     * Automatically assigns the first available room of the requested category,
     * persists the reservation to the database, and publishes a Kafka event.
     *
     * @param userId       the ID of the user making the reservation
     * @param categoryId   the ID of the requested room category
     * @param checkIn      the check-in date
     * @param checkOut     the check-out date
     * @param guestRequest specific requests or notes from the guest
     * @return the persisted Reservation entity
     * @throws RuntimeException if no rooms are available for the selected dates
     */
    @Transactional
    public Reservation bookRoom(Long userId, Long categoryId, LocalDate checkIn, LocalDate checkOut, String guestRequest) {

        List<Room> availableRooms = checkAvailability(categoryId, checkIn, checkOut);

        if (availableRooms.isEmpty()) {
            throw new RuntimeException("No available rooms found for this category on the specified dates.");
        }

        Room roomToBook = availableRooms.get(0);

        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setReservationStatus(EReservationStatus.PENDING); 

        RoomBooking roomBooking = new RoomBooking();
        roomBooking.setRoom(roomToBook);
        roomBooking.setCheckInDate(checkIn);
        roomBooking.setCheckOutDate(checkOut);
        roomBooking.setGuestRequest(guestRequest);

        reservation.addRoomBooking(roomBooking);

        Reservation savedReservation = reservationRepo.save(reservation);

        log.info("Successfully created reservation {} for User {}", savedReservation.getId(), userId);

        ReservationCreatedEvent event = new ReservationCreatedEvent(
            savedReservation.getId(),
            userId,
            savedReservation.getReservationStatus().name()
        );

        // Use the reservation ID as the message key to ensure ordered processing for the same reservation
        kafkaTemplate.send(RESERVATION_TOPIC, String.valueOf(savedReservation.getId()), event);

        return savedReservation;
    }

    /**
     * Gets all room bookings for a specific date range to render the frontend chessboard.
     */
    public List<RoomBooking> getBookingsForCalendar(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching calendar data from {} to {}", startDate, endDate);
        return roomBookingRepo.findBookingsInRange(startDate, endDate);
    }

}