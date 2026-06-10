package org.settlehub.booking.core.api;

import lombok.RequiredArgsConstructor;

import org.settlehub.booking.core.domain.ERoomStatus;
import org.settlehub.booking.core.domain.Room;
import org.settlehub.booking.core.logic.HousekeepingLogic;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for housekeeping staff mobile interface.
 */
@RestController
@RequestMapping("/housekeeping")
@RequiredArgsConstructor
public class HousekeepingController {

    private final HousekeepingLogic housekeepingLogic;

    /**
     * GET /booking/api/housekeeping/dirty?floor=2
     * Retrieves a list of dirty rooms.
     */
    @GetMapping("/dirty")
    public ResponseEntity<List<Room>> getDirtyRooms(@RequestParam(required = false) Integer floor) {
        return ResponseEntity.ok(housekeepingLogic.getDirtyRooms(floor));
    }

    /**
     * PATCH /booking/api/housekeeping/rooms/101/clean
     * Marks a specific room as cleaned and available.
     */
    @PatchMapping("/rooms/{roomId}/clean")
    public ResponseEntity<Room> cleanRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(housekeepingLogic.markRoomAsCleaned(roomId));
    }

    /**
     * PATCH /booking/api/housekeeping/rooms/101/dirty
     * Manually marks a specific room as dirty.
     */
    @PatchMapping("/rooms/{roomId}/dirty")
    public ResponseEntity<Room> dirtyRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(housekeepingLogic.markRoomAsDirty(roomId));
    }

    /**
     * PUT /booking/api/housekeeping/rooms/101/status?newStatus=OUT_OF_ORDER
     * Manually updates the status of any room.
     */
    @PutMapping("/rooms/{roomId}/status")
    public ResponseEntity<Room> updateRoomStatus(
            @PathVariable Long roomId,
            @RequestParam ERoomStatus newStatus) {
        return ResponseEntity.ok(housekeepingLogic.changeRoomStatus(roomId, newStatus));
    }

}
