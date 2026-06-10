package org.settlehub.booking.core.repo;

import org.settlehub.booking.core.domain.RoomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomCategoryRepo extends JpaRepository<RoomCategory, Long> {}
