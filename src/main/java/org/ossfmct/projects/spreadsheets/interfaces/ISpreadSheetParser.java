package org.ossfmct.projects.spreadsheets.interfaces;

import org.apache.poi.ss.usermodel.Cell;
import org.ossfmct.projects.spreadsheets.models.Resident;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface ISpreadSheetParser {

    Set<Resident> getResidentsData(String sheetName) throws IOException;

    Resident parseResidentFromList(List<Cell> list) throws RuntimeException, IllegalArgumentException;

    Date getCacheLastUpdate();
}
