package org.settlehub.booking.core.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.settlehub.booking.core.domain.HousekeepingRule;

@Repository
public interface HousekeepingRuleRepo extends JpaRepository<HousekeepingRule, Long> {}
