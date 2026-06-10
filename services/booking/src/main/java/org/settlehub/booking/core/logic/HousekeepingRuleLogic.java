package org.settlehub.booking.core.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.settlehub.booking.core.domain.HousekeepingRule;
import org.settlehub.booking.core.domain.RoomCategory;
import org.settlehub.booking.core.repo.HousekeepingRuleRepo;
import org.settlehub.booking.core.repo.RoomCategoryRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service logic for managing dynamic Housekeeping Rules.
 * Allows administrators to configure automated cleaning schedules.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HousekeepingRuleLogic {

    private final HousekeepingRuleRepo ruleRepo;
    private final RoomCategoryRepo categoryRepo;

    /**
     * Creates a new housekeeping rule.
     * Optionally links it to a specific room category if a category ID is provided.
     *
     * @param rule the rule details to create
     * @param categoryId the optional target category ID (can be null for global rules)
     * @return the saved HousekeepingRule entity
     */
    @Transactional
    public HousekeepingRule createRule(HousekeepingRule rule, Long categoryId) {
        log.info("Creating new housekeeping rule: {}", rule.getName());
        
        if (categoryId != null) {
            RoomCategory targetCategory = categoryRepo.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
            rule.setTargetCategory(targetCategory);
        } else {
            rule.setTargetCategory(null);
        }
        
        return ruleRepo.save(rule);
    }

    /**
     * Retrieves all configured housekeeping rules.
     *
     * @return list of all rules
     */
    public List<HousekeepingRule> getAllRules() {
        log.info("Fetching all housekeeping rules");
        return ruleRepo.findAll();
    }

    /**
     * Updates an existing rule or changes its target category.
     *
     * @param id the ID of the rule to update
     * @param updatedDetails the new configuration parameters
     * @param categoryId the new target category ID (optional)
     * @return the updated HousekeepingRule entity
     */
    @Transactional
    public HousekeepingRule updateRule(Long id, HousekeepingRule updatedDetails, Long categoryId) {
        log.info("Updating housekeeping rule with id: {}", id);
        HousekeepingRule existingRule = ruleRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found with id: " + id));

        existingRule.setName(updatedDetails.getName());
        existingRule.setTriggerType(updatedDetails.getTriggerType());
        existingRule.setExecutionTime(updatedDetails.getExecutionTime());
        existingRule.setDayOfWeek(updatedDetails.getDayOfWeek());

        if (categoryId != null) {
            RoomCategory targetCategory = categoryRepo.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
            existingRule.setTargetCategory(targetCategory);
        } else {
            existingRule.setTargetCategory(null);
        }

        return ruleRepo.save(existingRule);
    }

    /**
     * Toggles the active status of a specific rule.
     *
     * @param id the ID of the rule
     * @param isActive the new active state
     * @return the updated HousekeepingRule entity
     */
    @Transactional
    public HousekeepingRule toggleRuleStatus(Long id, boolean isActive) {
        log.info("Toggling rule {} active status to: {}", id, isActive);
        HousekeepingRule rule = ruleRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found with id: " + id));
        
        rule.setIsActive(isActive);
        return ruleRepo.save(rule);
    }

    /**
     * Deletes a housekeeping rule permanently.
     *
     * @param id the ID of the rule to delete
     */
    @Transactional
    public void deleteRule(Long id) {
        log.info("Deleting housekeeping rule with id: {}", id);
        ruleRepo.deleteById(id);
    }
}
