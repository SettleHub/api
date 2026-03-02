package org.ossfmct.projects.users.repositories;

import jakarta.validation.constraints.NotBlank;
import org.ossfmct.projects.users.models.BlankUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BlankUserRepository extends JpaRepository<BlankUser, Long> {

    Optional<BlankUser> findByEmail(String email);

    Boolean existsByEmail(String email);

    String email(@NotBlank String email);
}
