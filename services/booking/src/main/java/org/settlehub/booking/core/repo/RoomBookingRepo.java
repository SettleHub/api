package org.settlehub.booking.core.repo;

import org.settlehub.booking.core.domain.RoomBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomBookingRepo extends JpaRepository<RoomBooking, Long> {

    /**
     * Finds all room bookings that end on a specific date.
     *
     * @param checkOutDate the date of checkout
     * @return list of bookings matching the checkout date
     */
    List<RoomBooking> findByCheckOutDate(LocalDate checkOutDate);


    /**
     * Finds all room bookings that start on a specific date.
     *
     * @param checkInDate the date of checkin
     * @return list of bookings matching the checkin date
     */
    List<RoomBooking> findByCheckInDate(LocalDate checkInDate);


    /**
     * Retrieves all bookings that overlap with a given date range.
     * Essential for drawing the frontend booking calendar (chessboard).
     */
    @Query("SELECT rb FROM RoomBooking rb WHERE rb.checkInDate <= :endDate AND rb.checkOutDate >= :startDate")
    List<RoomBooking> findBookingsInRange(
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );

}
