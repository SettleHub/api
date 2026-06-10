package org.settlehub.booking.core.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.settlehub.booking.core.domain.ERoomStatus;
import org.settlehub.booking.core.domain.Room;
import org.settlehub.booking.core.repo.RoomRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service logic for hotel housekeeping operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HousekeepingLogic {

    private final RoomRepo roomRepo;

    /**
     * Retrieves all rooms currently marked as DIRTY.
     * Optionally filters by floor if provided.
     *
     * @param floor the floor number to filter by (can be null)
     * @return a list of dirty rooms
     */
    public List<Room> getDirtyRooms(Integer floor) {
        log.info("Fetching dirty rooms. Floor filter: {}", floor);
        List<Room> dirtyRooms = roomRepo.findByStatus(ERoomStatus.DIRTY);
        
        if (floor != null) {
            return dirtyRooms.stream()
                    .filter(room -> floor.equals(room.getFloor()))
                    .collect(Collectors.toList());
        }
        return dirtyRooms;
    }

    /**
     * Updates a room's status from DIRTY to AVAILABLE after cleaning.
     *
     * @param roomId the ID of the cleaned room
     * @return the updated Room entity
     */
    @Transactional
    public Room markRoomAsCleaned(Long roomId) {
        log.info("Marking room {} as cleaned", roomId);
        Room room = roomRepo.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        room.setStatus(ERoomStatus.AVAILABLE);
        return roomRepo.save(room);
    }

    /**
     * Updates a room's status as DIRTY.
     *
     * @param roomId the ID of the dirty room
     * @return the updated Room entity
     */
    @Transactional
    public Room markRoomAsDirty(Long roomId) {
        log.info("Marking room {} as dirty", roomId);
        Room room = roomRepo.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        room.setStatus(ERoomStatus.DIRTY);
        return roomRepo.save(room);
    }

    /**
     * Allows staff to manually override a room's status.
     * Useful for emergency cleanings or marking a room out of order.
     *
     * @param roomId the ID of the room
     * @param newStatus the new status to apply
     * @return the updated Room entity
     */
    @Transactional
    public Room changeRoomStatus(Long roomId, ERoomStatus newStatus) {
        log.info("Manually changing room {} status to {}", roomId, newStatus);
        Room room = roomRepo.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        room.setStatus(newStatus);
        return roomRepo.save(room);
    }

}
