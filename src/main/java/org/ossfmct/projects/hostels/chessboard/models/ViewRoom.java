package org.ossfmct.projects.hostels.chessboard.models;

import java.util.ArrayList;
import java.util.List;

import org.ossfmct.projects.spreadsheets.enums.Gender;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViewRoom {
    private String roomNumber;
    private boolean isAdditional;
    private List<Gender> badPlaces;

    public ViewRoom() {}

    public ViewRoom(String roomNumber, boolean isAdditional, List<Gender> badPlaces) {
        this.roomNumber = roomNumber;
        this.isAdditional = isAdditional;
        this.badPlaces = new ArrayList<>(badPlaces);
    }

    public void addBadPlace(Gender gender) {
        badPlaces.add(gender);
    }

    public void removeBadPlace(int index) {
        badPlaces.remove(index);
    }
}