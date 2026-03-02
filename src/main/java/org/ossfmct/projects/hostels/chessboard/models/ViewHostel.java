package org.ossfmct.projects.hostels.chessboard.models;

import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class ViewHostel {
    private Set<ViewFloor> floors;
    private Short firstRoom;
    private Short lastRoom;

    public ViewHostel() {}

    public ViewHostel(Set<ViewFloor> floors, Short firstRoom, Short lastRoom) {
        this.floors = floors;
        this.firstRoom = firstRoom;
        this.lastRoom = lastRoom;
    }

    public void addFloor(ViewFloor floor) {
        floors.add(floor);
    }

    public ViewFloor getFloor(Short floorNumber) {
        for (ViewFloor floor : floors) {
            if (floor.getFloorNumber().equals(floorNumber)) return floor;
        }
        return null;
    }

}
