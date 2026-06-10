package org.settlehub.booking.core.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.settlehub.booking.core.domain.Room;
import org.settlehub.booking.core.domain.RoomCategory;
import org.settlehub.booking.core.logic.RoomManagementLogic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller providing administrative endpoints for Room and Category Management (CRUD).
 */
@Slf4j
@RestController
@RequestMapping("/management")
@RequiredArgsConstructor
public class RoomManagementController {

    private final RoomManagementLogic roomManagementLogic;

    // ==========================================
    // ROOM CATEGORY ENDPOINTS
    // ==========================================

    /**
     * POST /booking/api/management/categories
     * Creates a new room category.
     */
    @PostMapping("/categories")
    public ResponseEntity<RoomCategory> createCategory(@RequestBody RoomCategory category) {
        RoomCategory created = roomManagementLogic.createCategory(category);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * GET /booking/api/management/categories
     * Returns a list of all room categories.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<RoomCategory>> getAllCategories() {
        return ResponseEntity.ok(roomManagementLogic.getAllCategories());
    }

    /**
     * GET /booking/api/management/categories/{id}
     * Returns details of a specific room category.
     */
    @GetMapping("/categories/{id}")
    public ResponseEntity<RoomCategory> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(roomManagementLogic.getCategoryById(id));
    }

    /**
     * PUT /booking/api/management/categories/{id}
     * Updates an existing room category configuration.
     */
    @PutMapping("/categories/{id}")
    public ResponseEntity<RoomCategory> updateCategory(
            @PathVariable Long id, 
            @RequestBody RoomCategory updatedDetails) {
        return ResponseEntity.ok(roomManagementLogic.updateCategory(id, updatedDetails));
    }

    /**
     * DELETE /booking/api/management/categories/{id}
     * Deletes a specific category.
     */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        roomManagementLogic.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // HOTEL ROOM ENDPOINTS
    // ==========================================

    /**
     * POST /booking/api/management/rooms?categoryId=1
     * Registers a new room into the hotel inventory.
     */
    @PostMapping("/rooms")
    public ResponseEntity<Room> createRoom(
            @RequestBody Room room, 
            @RequestParam Long categoryId) {
        Room created = roomManagementLogic.createRoom(room, categoryId);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * GET /booking/api/management/rooms
     * Returns a list of all hotel rooms.
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomManagementLogic.getAllRooms());
    }

    /**
     * GET /booking/api/management/rooms/{id}
     * Returns details of a specific hotel room.
     */
    @GetMapping("/rooms/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomManagementLogic.getRoomById(id));
    }

    /**
     * PUT /booking/api/management/rooms/{id}?categoryId=2
     * Updates an existing hotel room parameters or swaps its category.
     */
    @PutMapping("/rooms/{id}")
    public ResponseEntity<Room> updateRoom(
            @PathVariable Long id, 
            @RequestBody Room updatedDetails, 
            @RequestParam Long categoryId) {
        return ResponseEntity.ok(roomManagementLogic.updateRoom(id, updatedDetails, categoryId));
    }

    /**
     * DELETE /booking/api/management/rooms/{id}
     * Removes a room from the inventory dataset.
     */
    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomManagementLogic.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
