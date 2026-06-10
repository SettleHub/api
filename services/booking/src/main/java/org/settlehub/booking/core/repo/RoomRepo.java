package org.settlehub.booking.core.repo;

import org.settlehub.booking.core.domain.Room;
import org.settlehub.booking.core.domain.ERoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepo extends JpaRepository<Room, Long> {
    
    @Query( "SELECT r FROM Room r WHERE r.category.id = :categoryId " +
            "AND r.id NOT IN (" +
            "  SELECT rb.room.id FROM RoomBooking rb " +
            "  WHERE rb.checkInDate < :checkOutDate AND rb.checkOutDate > :checkInDate" +
            ")" )
    List<Room> findAvailableRooms(
        @Param("categoryId") Long categoryId,
        @Param("checkInDate") LocalDate checkInDate,
        @Param("checkOutDate") LocalDate checkOutDate
    );

    List<Room> findByStatus(ERoomStatus status);

}
