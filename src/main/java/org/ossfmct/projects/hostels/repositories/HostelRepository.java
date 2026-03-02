package org.ossfmct.projects.hostels.repositories;

import jakarta.transaction.Transactional;
import org.ossfmct.projects.hostels.models.Hostel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HostelRepository extends JpaRepository<Hostel, Long> {
    /**
     * Method to find needed hostel by his number.
     * @param number of hostel.
     * @return Optional hostel object.
     */
    Optional<Hostel> findByNumber(Integer number);

    @Transactional
    default Hostel updateOrInsert(Hostel entity) {
        return save(entity);
    }
}