package org.settlehub.booking.core.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false)
    private EReservationStatus reservationStatus;

    @Column(name = "general_guest_request")
    private String generalGuestRequest;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomBooking> roomBookings = new ArrayList<>();

    public void addRoomBooking(RoomBooking booking) {
        roomBookings.add(booking);
        booking.setReservation(this);
    }

    public void removeRoomBooking(RoomBooking booking) {
        roomBookings.remove(booking);
        booking.setReservation(null);
    }
}