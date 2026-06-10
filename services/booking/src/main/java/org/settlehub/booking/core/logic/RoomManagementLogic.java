package org.settlehub.booking.core.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.settlehub.booking.core.domain.Room;
import org.settlehub.booking.core.domain.RoomCategory;
import org.settlehub.booking.core.repo.RoomCategoryRepo;
import org.settlehub.booking.core.repo.RoomRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service logic layer for managing hotel rooms and room categories (CRUD).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomManagementLogic {

    private final RoomRepo roomRepo;
    private final RoomCategoryRepo roomCategoryRepo;

    // ==========================================
    // ROOM CATEGORY CRUD OPERATIONS
    // ==========================================

    /**
     * Creates and persists a new room category.
     *
     * @param category the room category details to create
     * @return the saved RoomCategory entity
     */
    @Transactional
    public RoomCategory createCategory(RoomCategory category) {
        log.info("Creating a new room category: {}", category.getName());
        return roomCategoryRepo.save(category);
    }

    /**
     * Retrieves all available room categories.
     *
     * @return a list of all room categories
     */
    public List<RoomCategory> getAllCategories() {
        log.info("Fetching all room categories");
        return roomCategoryRepo.findAll();
    }

    /**
     * Finds a specific room category by its unique ID.
     *
     * @param id the ID of the category
     * @return the found RoomCategory entity
     * @throws RuntimeException if the category is not found
     */
    public RoomCategory getCategoryById(Long id) {
        log.info("Fetching room category by id: {}", id);
        return roomCategoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Room category not found with id: " + id));
    }

    /**
     * Updates an existing room category with new details.
     *
     * @param id             the ID of the category to update
     * @param updatedDetails the new details to apply
     * @return the updated RoomCategory entity
     */
    @Transactional
    public RoomCategory updateCategory(Long id, RoomCategory updatedDetails) {
        log.info("Updating room category with id: {}", id);
        RoomCategory existingCategory = getCategoryById(id);

        existingCategory.setName(updatedDetails.getName());
        existingCategory.setBasePrice(updatedDetails.getBasePrice());
        existingCategory.setMaxAdults(updatedDetails.getMaxAdults());
        existingCategory.setMaxChildren(updatedDetails.getMaxChildren());

        return roomCategoryRepo.save(existingCategory);
    }

    /**
     * Deletes a room category from the database by its ID.
     *
     * @param id the ID of the category to delete
     */
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting room category with id: {}", id);
        RoomCategory category = getCategoryById(id);
        roomCategoryRepo.delete(category);
    }

    // ==========================================
    // HOOTEL ROOM CRUD OPERATIONS
    // ==========================================

    /**
     * Creates a new hotel room and associates it with a specific category.
     *
     * @param room       the room details to create
     * @param categoryId the ID of the category this room belongs to
     * @return the saved Room entity
     */
    @Transactional
    public Room createRoom(Room room, Long categoryId) {
        log.info("Creating a new room number: {} under category id: {}", room.getNumber(), categoryId);
        RoomCategory category = getCategoryById(categoryId);
        room.setCategory(category);
        return roomRepo.save(room);
    }

    /**
     * Retrieves all hotel rooms inside the system.
     *
     * @return a list of all rooms
     */
    public List<Room> getAllRooms() {
        log.info("Fetching all hotel rooms");
        return roomRepo.findAll();
    }

    /**
     * Finds a specific room by its unique ID.
     *
     * @param id the ID of the room
     * @return the found Room entity
     * @throws RuntimeException if the room is not found
     */
    public Room getRoomById(Long id) {
        log.info("Fetching room by id: {}", id);
        return roomRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
    }

    /**
     * Updates an existing hotel room configuration and its status.
     *
     * @param id             the ID of the room to update
     * @param updatedDetails the new room values to map
     * @param categoryId     the new category ID association
     * @return the updated Room entity
     */
    @Transactional
    public Room updateRoom(Long id, Room updatedDetails, Long categoryId) {
        log.info("Updating room with id: {}, mapping to category id: {}", id, categoryId);
        Room existingRoom = getRoomById(id);
        RoomCategory category = getCategoryById(categoryId);

        existingRoom.setNumber(updatedDetails.getNumber());
        existingRoom.setStatus(updatedDetails.getStatus());
        existingRoom.setFloor(updatedDetails.getFloor());
        existingRoom.setBuilding(updatedDetails.getBuilding());
        existingRoom.setBranchId(updatedDetails.getBranchId());
        existingRoom.setCategory(category);

        return roomRepo.save(existingRoom);
    }

    /**
     * Deletes a hotel room from the system.
     *
     * @param id the ID of the room to remove
     */
    @Transactional
    public void deleteRoom(Long id) {
        log.info("Deleting room with id: {}", id);
        Room room = getRoomById(id);
        roomRepo.delete(room);
    }
}
