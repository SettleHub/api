package org.ossfmct.projects.spreadsheets.interfaces;

import org.ossfmct.projects.spreadsheets.models.Resident;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

public interface ISpreadSheetService {

    Set<Resident> getResidents(String sheetName) throws IOException, RuntimeException;

    Optional<Resident> getResident(String sheetName, String firstName, String lastName) throws IOException, RuntimeException;

    Optional<Resident> getResident(String sheetName, String contractNumber) throws IOException, RuntimeException;

    Optional<Resident> getResident(String sheetName, short bedPlace) throws IOException, RuntimeException;

    Set<Resident> getResidentsFromRoom(String sheetName, String roomNumber) throws IOException, RuntimeException;

    Date getLastUpdateWas();

}
