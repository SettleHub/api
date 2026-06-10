package org.settlehub.booking.core.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.settlehub.booking.core.domain.Reservation;

@Repository
public interface ReservationRepo extends JpaRepository<Reservation, Long> {}
