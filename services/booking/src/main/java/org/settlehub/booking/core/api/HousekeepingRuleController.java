package org.settlehub.booking.core.api;

import lombok.RequiredArgsConstructor;
import org.settlehub.booking.core.domain.HousekeepingRule;
import org.settlehub.booking.core.logic.HousekeepingRuleLogic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Housekeeping Automation Rules.
 * Accessed primarily by hotel administrators.
 */
@RestController
@RequestMapping("/management/housekeeping-rules")
@RequiredArgsConstructor
public class HousekeepingRuleController {

    private final HousekeepingRuleLogic ruleLogic;

    /**
     * POST /booking/api/management/housekeeping-rules?categoryId=1
     * Creates a new automated cleaning rule.
     */
    @PostMapping
    public ResponseEntity<HousekeepingRule> createRule(
            @RequestBody HousekeepingRule rule,
            @RequestParam(required = false) Long categoryId) {
        HousekeepingRule created = ruleLogic.createRule(rule, categoryId);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * GET /booking/api/management/housekeeping-rules
     * Retrieves all existing rules.
     */
    @GetMapping
    public ResponseEntity<List<HousekeepingRule>> getAllRules() {
        return ResponseEntity.ok(ruleLogic.getAllRules());
    }

    /**
     * PUT /booking/api/management/housekeeping-rules/{id}?categoryId=2
     * Fully updates an existing rule's configuration.
     */
    @PutMapping("/{id}")
    public ResponseEntity<HousekeepingRule> updateRule(
            @PathVariable Long id,
            @RequestBody HousekeepingRule ruleDetails,
            @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(ruleLogic.updateRule(id, ruleDetails, categoryId));
    }

    /**
     * PATCH /booking/api/management/housekeeping-rules/{id}/status?isActive=false
     * Quickly enables or disables a rule without modifying other parameters.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<HousekeepingRule> toggleRuleStatus(
            @PathVariable Long id,
            @RequestParam boolean isActive) {
        return ResponseEntity.ok(ruleLogic.toggleRuleStatus(id, isActive));
    }

    /**
     * DELETE /booking/api/management/housekeeping-rules/{id}
     * Removes a rule from the system.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        ruleLogic.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}
