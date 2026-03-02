package org.ossfmct.projects.hostels.chessboard;

import org.ossfmct.projects.hostels.chessboard.config.HostelThreeViewConfiguration;
import org.ossfmct.projects.hostels.chessboard.interfaces.IHostelViewService;
import org.ossfmct.projects.hostels.chessboard.models.ViewFloor;
import org.ossfmct.projects.hostels.chessboard.models.ViewHostel;
import org.ossfmct.projects.hostels.chessboard.models.ViewRoom;
import org.ossfmct.projects.spreadsheets.enums.Gender;
import org.ossfmct.projects.spreadsheets.interfaces.ISpreadSheetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;

@Service
public class HostelViewService implements IHostelViewService {
    private final ISpreadSheetService spreadSheetService;
    private final Logger logger = LoggerFactory.getLogger(HostelViewService.class);
    private static final Short SETTLED_FLOORS_FROM = 2;
    private final HostelThreeViewConfiguration hostelThreeViewConfiguration;

    public HostelViewService(ISpreadSheetService spreadSheetService, HostelThreeViewConfiguration hostelThreeViewConfiguration) {
        this.spreadSheetService = spreadSheetService;
        this.hostelThreeViewConfiguration = hostelThreeViewConfiguration;
    }

    @Override
    public ViewHostel getViewHostel(String hostelNumber) throws IOException {
        if (hostelThreeViewConfiguration.getHostelNumber().equals(hostelNumber)) {
            return generateViewHostelsWithoutGenders(
                    hostelThreeViewConfiguration.getFirstRoom(),
                    hostelThreeViewConfiguration.getLastRoom(),
                    hostelThreeViewConfiguration.getRoomsCountOnFloor(),
                    hostelThreeViewConfiguration.getAdditionalRoomSymbol(),
                    hostelThreeViewConfiguration.getAdditionalRooms());
        } else {
            return null;
        }
    }

    private ViewHostel generateViewHostelsWithoutGenders(Short firstRoom, Short lastRoom, Short roomsCountOnFloor, String additionalRoomSymbol, String additionalRooms) throws IOException {
        Set<Short> additionalRoomsNumbers = parseAdditionalRooms(additionalRooms, additionalRoomSymbol);
        ViewHostel viewHostel = new ViewHostel(new LinkedHashSet<>(), firstRoom, lastRoom);
        short lastFloor = Short.parseShort(Integer.toString(Math.round((float) (lastRoom - firstRoom) / roomsCountOnFloor) + SETTLED_FLOORS_FROM - 1));
        for (short floorIndex = SETTLED_FLOORS_FROM; floorIndex <= lastFloor; floorIndex++) {
            viewHostel.addFloor(new ViewFloor(floorIndex, new ArrayList<>()));
        }
        for (short roomNumber = firstRoom; roomNumber <= lastRoom; roomNumber++) {
            short floorIndex = determineFloor(roomNumber, firstRoom, roomsCountOnFloor);
            ViewFloor viewFloor = viewHostel.getFloor(floorIndex);
            viewFloor.addRoom(new ViewRoom(Short.toString(roomNumber), false, List.of(
                    Gender.NOT_SPECIFIED,
                    Gender.NOT_SPECIFIED,
                    Gender.NOT_SPECIFIED,
                    Gender.NOT_SPECIFIED)));
            for (Short additionalRoom : additionalRoomsNumbers) {
                if (additionalRoom.equals(roomNumber)) {
                    viewFloor.addRoom(new ViewRoom(String.format("%s%s", roomNumber, additionalRoomSymbol), true, List.of(
                            Gender.NOT_SPECIFIED,
                            Gender.NOT_SPECIFIED,
                            Gender.NOT_SPECIFIED,
                            Gender.NOT_SPECIFIED)));
                    break;
                }
            }
        }
        return viewHostel;
    }

    private Set<Short> parseAdditionalRooms(String additionalRooms, String additionalRoomSymbol) {
        String[] arrayOfAdditionalRooms = additionalRooms.trim().split(",");
        for (int i = 0; i < arrayOfAdditionalRooms.length; i++) {
            arrayOfAdditionalRooms[i] = arrayOfAdditionalRooms[i].trim().replace(additionalRoomSymbol, "");
        }
        Set<Short> additionalRoomsNumbers = new LinkedHashSet<>();
        for (String str : arrayOfAdditionalRooms) {
            additionalRoomsNumbers.add(Short.parseShort(str));
        }
        return additionalRoomsNumbers;
    }

    private short determineFloor(short roomNumber, short firstRoom, short roomsCountOnFloor) {
        return (short) (((roomNumber - firstRoom) / roomsCountOnFloor) + SETTLED_FLOORS_FROM);
    }
}