package org.settlehub.booking.core.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalTime;
import java.time.DayOfWeek;

/**
 * Enterprise Rule-Based Engine entity for Housekeeping.
 * Allows hotel administrators to dynamically construct cleaning schedules.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "housekeeping_rules")
public class HousekeepingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private ECleaningTrigger triggerType;

    @Column(name = "execution_time", nullable = false)
    private LocalTime executionTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    private RoomCategory targetCategory;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

}
