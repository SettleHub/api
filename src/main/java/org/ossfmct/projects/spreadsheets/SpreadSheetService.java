package org.ossfmct.projects.spreadsheets;

import org.ossfmct.projects.spreadsheets.interfaces.ISpreadSheetParser;
import org.ossfmct.projects.spreadsheets.interfaces.ISpreadSheetService;
import org.ossfmct.projects.spreadsheets.models.Resident;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Component
public class SpreadSheetService implements ISpreadSheetService {
    private final ISpreadSheetParser spreadSheetParser;

    public SpreadSheetService(ISpreadSheetParser spreadSheetParser) {
        this.spreadSheetParser = spreadSheetParser;
    }

    @Override
    public Set<Resident> getResidents(String sheetName) throws IOException, RuntimeException {
        return spreadSheetParser.getResidentsData(sheetName);
    }

    @Override
    public Optional<Resident> getResident(String sheetName, String firstName, String lastName) throws IOException, RuntimeException {
        return spreadSheetParser.getResidentsData(sheetName).stream().
            filter(resident -> (
                resident.getFirstName().equalsIgnoreCase(firstName)
                && resident.getLastName().equalsIgnoreCase(lastName))).
            findFirst();
    }

    @Override
    public Optional<Resident> getResident(String sheetName, String contractNumber) throws IOException, RuntimeException {
        return spreadSheetParser.getResidentsData(sheetName).stream().
            filter(resident -> (
                resident.getContractNumber().equalsIgnoreCase(contractNumber))).
            findFirst();
    }

    @Override
    public Optional<Resident> getResident(String sheetName, short bedPlace) throws IOException, RuntimeException {
        return spreadSheetParser.getResidentsData(sheetName).stream().
            filter(resident -> (
                resident.getBedPlace() == bedPlace)).
            findFirst();
    }

    @Override
    public Set<Resident> getResidentsFromRoom(String sheetName, String roomNumber) throws IOException, RuntimeException {
        return spreadSheetParser.getResidentsData(sheetName).stream()
            .filter(resident -> resident.getRoomNumber().equalsIgnoreCase(roomNumber))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Date getLastUpdateWas() {
        return spreadSheetParser.getCacheLastUpdate();
    }
}
