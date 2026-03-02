package org.ossfmct.projects.hostels.chessboard.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ViewFloor {
    private Short floorNumber;
    private List<ViewRoom> rooms;

    public ViewFloor() {}

    public ViewFloor(Short floorNumber, List<ViewRoom> rooms) {
        this.floorNumber = floorNumber;
        this.rooms = rooms;
    }

    public void addRoom(ViewRoom room) {
        rooms.add(room);
    }

}
