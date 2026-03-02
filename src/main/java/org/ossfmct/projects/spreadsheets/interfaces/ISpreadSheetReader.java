package org.ossfmct.projects.spreadsheets.interfaces;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ISpreadSheetReader {

    void changeWorkbook() throws IOException;

    void changeWorkbook(File file) throws IOException;

    FormulaEvaluator getFormulaEvaluator() throws IOException;

    void readSheets() throws IOException;

    void checkSheets() throws IOException;

    void checkSheets(String name) throws IOException, RuntimeException;

    List<Sheet> getSheets() throws IOException;

    Sheet getSheet(String name) throws IOException;

    Map<Integer, Row> getRows(Sheet sheet) throws IOException, RuntimeException;

    Triple<Date, String, Map<Integer, List<Cell>>> getCells(Sheet sheet, CellReference fromRef) throws RuntimeException;

    Cell getCell(Sheet sheet, CellReference reference) throws RuntimeException;

    Cell getCell(Sheet sheet, int rowIndex, int cellIndex) throws RuntimeException;

}
