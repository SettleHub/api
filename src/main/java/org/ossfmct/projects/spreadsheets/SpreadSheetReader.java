package org.ossfmct.projects.spreadsheets;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.javatuples.Pair;
import org.ossfmct.projects.spreadsheets.interfaces.ISpreadSheetReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import org.ossfmct.projects.tools.interfaces.IResourceLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SpreadSheetReader implements ISpreadSheetReader {
    private static final String SPREADSHEET_FILENAME = "classpath:settlement_table.xlsx";
    private static Workbook workbook;
    private static Pair<List<Sheet>, LocalDate> sheets;
    private static List<Pair<Integer, String>> sheetNames;
    private final IResourceLoaderService resourceLoaderService;
    private final Logger logger = LoggerFactory.getLogger(SpreadSheetReader.class);

    public SpreadSheetReader(IResourceLoaderService resourceLoaderService) throws IOException {
        this.resourceLoaderService = resourceLoaderService;
        if (workbook == null) {
            workbook = new XSSFWorkbook(resourceLoaderService.getInputStreamFromResourceFile(SPREADSHEET_FILENAME));
        }
        if (sheetNames == null) {
            sheetNames = new ArrayList<Pair<Integer, String>>();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheetNames.add(Pair.with(i, workbook.getSheetAt(i).getSheetName()));
            }
        }
        readSheets();
    }

    public List<Sheet> getSheets() throws IOException  {
        checkSheets();
        return sheets.getValue0();
    }

    /**
     *
     * @param name
     * @return org.apache.poi.ss.usermodel.Sheet if there is an object with this name in the list and null if not found.
     * @throws IOException
     * @throws RuntimeException
     */
    public Sheet getSheet(String name) throws IOException, RuntimeException  {
        checkSheets(name);
        for (Sheet sheet : sheets.getValue0()) {
            if (sheet.getSheetName().equals(name)) return sheet;
        }
        throw new RuntimeException("Sheet with name=" + name + "was not found");
    }

    public Cell getCell(Sheet sheet, CellReference reference) throws RuntimeException {
        Cell cell = sheet.getRow(reference.getRow()).getCell(reference.getCol());
        if (cell != null) {
            return cell;
        } else {
            throw new RuntimeException("Cell from reference=" + reference.formatAsString() + "was not found");
        }
    }

    public Cell getCell(Sheet sheet, int rowIndex, int cellIndex) throws RuntimeException {
        Cell cell = sheet.getRow(rowIndex).getCell(cellIndex);
        if (cell != null) {
            return cell;
        } else {
            throw new RuntimeException("Cell from row=" + rowIndex + " with cell index=" + cellIndex + "was not found");
        }
    }

    /**
     *
     * @param sheet
     * @param fromRef
     * @return Triple of Date of reading, String name of sheet and List of cells
     * @throws RuntimeException
     */
    public Triple<Date, String, Map<Integer, List<Cell>>> getCells(Sheet sheet, CellReference fromRef) throws RuntimeException {
        int fromRowIndex = fromRef.getRow();
        int fromColIndex = fromRef.getCol();
        Map<Integer, List<Cell>> rowsOfcells = new HashMap<>();

        for (int i = fromRowIndex; i < Integer.MAX_VALUE; i++) {
            List<Cell> listOfSingleRowOfcells = new ArrayList<>();
            Row row = sheet.getRow(i);
            if (row != null) {
                for (int j = fromColIndex; j < Integer.MAX_VALUE; j++) {
                    Cell cell = sheet.getRow(i).getCell(j);
                    if (cell != null) {
                        listOfSingleRowOfcells.add(cell);
                    } else {
                        break;
                    }
                }
                rowsOfcells.put(i, listOfSingleRowOfcells);
            } else {
                break;
            }
        }

        return Triple.of(Date.from(Instant.now()), sheet.getSheetName(), rowsOfcells);
    }

    public Map<Integer, Row> getRows(Sheet sheet) throws IOException, RuntimeException {
        checkSheets(sheet);
        Map<Integer, Row> data = new HashMap<Integer, Row>();
        int index = 0;
        for (Row row : sheet) {
            data.put(index, row);
            index++;
        }
        return data;
    }

    @Override
    public void changeWorkbook() throws IOException {
        workbook = new XSSFWorkbook(resourceLoaderService.getInputStreamFromResourceFile(SPREADSHEET_FILENAME));
        logger.info("Spreadsheet was changed to {}", SPREADSHEET_FILENAME);
    }
    @Override
    public void changeWorkbook(File file) throws IOException {
        workbook = new XSSFWorkbook(new FileInputStream(file));
        logger.info("Spreadsheet was changed to {}", file.getName());
    }

    @Override
    public FormulaEvaluator getFormulaEvaluator() throws IOException {
        return workbook.getCreationHelper().createFormulaEvaluator();
    }

    public void checkSheets() throws IOException {
        if (sheets == null || sheets.getSize() == 0) {
            readSheets();
        }
    }

    public void checkSheets(String sheetName) throws IOException {
        if (sheets == null) {
            readSheets();
        } else {
            boolean hasASheet = false;
            for (Sheet sheet : sheets.getValue0()) {
                if (sheet.getSheetName().equals(sheetName)) {
                    hasASheet = true;
                }
            }
            if (!hasASheet) readSheets();
        }
    }

    private void checkSheets(Sheet sheet) throws IOException, RuntimeException {
        if (sheets == null) {
            readSheets();
        } else {
            boolean hasASheet = false;
            for (Sheet s : sheets.getValue0()) {
                if (s.equals(sheet)) {
                    hasASheet = true;
                } else if (s.getSheetName().equals(sheet.getSheetName()) && !s.equals(sheet)) {
                    throw new RuntimeException("Sheets: " + s.getSheetName() + " and " + sheet.getSheetName() + " have the same name but different content");
                }
            }
            if (!hasASheet) readSheets();
        }
    }

    public void readSheets() throws IOException {
        List<Sheet> listOfSheets = new ArrayList<Sheet>();
        if (readSheetsFromWorkbook(listOfSheets) > 0) {
            sheets = Pair.with(listOfSheets, LocalDate.now(ZoneId.of("Europe/Kyiv")));
            logger.info("Reading sheets from a workbook is completed correctly.");
        } else {
            changeWorkbook();
            if (readSheetsFromWorkbook(listOfSheets) == 0) {
                logger.error("Reading sheets from workbook failed. Missing sheets.");
            }
        }
    }

    private int readSheetsFromWorkbook(List<Sheet> listOfSheets) {
        int readedSheets = 0;
        for (Pair<Integer, String> sheetNameFromList : sheetNames) {
            if (workbook.getSheet(sheetNameFromList.getValue1()) == null) {
                logger.error("Cannot find sheet with name: '{}' in workbook.", sheetNameFromList.getValue1());
            } else {
                listOfSheets.add(workbook.getSheet(sheetNameFromList.getValue1()));
                readedSheets++;
            }
        }
        return readedSheets;
    }

    @Override
    protected void finalize() throws IOException {
        logger.info("SpreadSheetReader object finalizing, closing the workbook: {}", workbook);
        workbook.close();
    }
}
