package org.ossfmct.projects.spreadsheets;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.util.CellReference;
import org.ossfmct.projects.spreadsheets.enums.Gender;
import org.ossfmct.projects.spreadsheets.interfaces.ISpreadSheetParser;
import org.ossfmct.projects.spreadsheets.interfaces.ISpreadSheetReader;
import org.ossfmct.projects.spreadsheets.models.Resident;
import org.ossfmct.projects.spreadsheets.config.SettlementSheetConfig;
import org.ossfmct.projects.tools.interfaces.IResourceLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Component
public class SpreadSheetParser implements ISpreadSheetParser {
    private final Logger logger = LoggerFactory.getLogger(SpreadSheetParser.class);
    private final ISpreadSheetReader spreadSheetReader;
    private final IResourceLoaderService resourceLoaderService;
    @Autowired
    private SettlementSheetConfig settlementSheetConfig;
    private final FormulaEvaluator formulaEvaluator;

    /**
     *
     * cache contain Date of parsing as a key and tuple of sheetName, parsed number of sheetName and Set of Residents
     */
    @Getter
    protected Map<Date, Triple<String, Integer, Set<Resident>>> cache = new HashMap<>();
    @Getter
    protected Date cacheLastUpdate;

    public SpreadSheetParser(ISpreadSheetReader spreadSheetReader, IResourceLoaderService resourceLoaderService) throws IOException, StreamReadException, DatabindException, IllegalArgumentException {
        this.spreadSheetReader = spreadSheetReader;
        this.resourceLoaderService = resourceLoaderService;
        this.formulaEvaluator = spreadSheetReader.getFormulaEvaluator();
        logger.info("Creating spread sheet parser object");
    }

    /**
     *
     * @param sheetName
     * @return Cached data if last update was less than 5 minutes ago
     * @throws IOException
     */
    @Override
    public Set<Resident> getResidentsData(String sheetName) throws IOException {
        if (cacheContain(sheetName)) {
            long timeSinceLastUpdate = System.currentTimeMillis() - cacheLastUpdate.getTime();
            long minutesSinceLastUpdate = TimeUnit.MILLISECONDS.toMinutes(timeSinceLastUpdate);

            // Cache can be updated only 5 minutes after previous update.
            if (minutesSinceLastUpdate < 5) {
                logger.info("Using cached data for sheet: {}", sheetName);
                return cache.get(cacheLastUpdate).getRight();
            } else {
                logger.info("Cache expired for sheet: {}, refreshing data.", sheetName);
            }
        } else {
            logger.info("Cache does not contain data for sheet: {}, fetching new data.", sheetName);
        }

        return parseResidentsDataFrom(sheetName);
    }

    private Set<Resident> parseResidentsDataFrom(String sheetName) throws IOException, RuntimeException {
        if (settlementSheetConfig != null) {
            Triple<Date, String, Map<Integer, List<Cell>>> cells = spreadSheetReader.getCells(
                    spreadSheetReader.getSheet(sheetName),
                    new CellReference(settlementSheetConfig.getBedPlace())
            );
            Set<Resident> residents = new LinkedHashSet<>();
            cells.getRight().forEach((key, list) -> {
                residents.add(parseResidentFromList(list));
            });
            correctResidentsSet(residents);

            cache.put(cells.getLeft(), Triple.of(
                    sheetName,
                    Integer.valueOf(cells.getMiddle().substring(cells.getMiddle().length() - 1)),
                    residents));
            cacheLastUpdate = cells.getLeft();

            return residents;
        } else {
            logger.error("Settlement Sheet Config value is null");
            throw new NullPointerException("Settlement Sheet Config value is null");
        }
    }

    /**
     *
     * @param list is a row of cells with resident data
     * @return parsed resident data
     * @throws RuntimeException
     * @throws IllegalArgumentException
     */
    @Override
    public Resident parseResidentFromList(List<Cell> list) throws RuntimeException, IllegalArgumentException {
        Triple<String, String, String> names = resolveFullName(
            resolveCell(list.get(2))
        );
        Resident resident = new Resident(
            parseShortFromCell(list.get(0), "0.0"),                                      // short bedPlace
            resolveCell(list.get(1)),                                                               // String roomNumber
            names.getMiddle(),                                                                      // String firstName
            names.getLeft(),                                                                        // String lastName
            names.getRight(),                                                                       // String middleName
            resolveGender((String) resolveCell(list.get(3), Gender.NOT_SPECIFIED.toString())),      // Gender gender
            resolveCell(list.get(4)),                                                               // String universityFacultyInstitute
            resolveCell(list.get(5)),                                                               // String courseAndGroup
            resolveCell(list.get(6)),                                                               // String contractNumber
            resolveCell(list.get(7)),                                                               // String contactPhone
            resolveCell(list.get(8)),                                                               // String applicationPlan2023_24
            parseShortFromCell(list.get(9), "0.0"),                                      // short settled
            parseShortFromCell(list.get(10), "0.0")                                      // short places
        );
        return resident;
    }

    /**
     *
     * @param fullName may be empty or contains Last Name and additional First Name and Middle Name at the end.
     * @return Triple of <LastName, FirstName, MiddleName>
     */
    private Triple<String, String, String> resolveFullName(String fullName) throws RuntimeException {
        String[] namesArray = fullName.split(" ");
        if (namesArray.length > 3) {
            logger.warn("Cell full name contain more than 3 word; Cell data: {}", fullName);
        }
        String leftValue = "";
        String middleValue = "";
        String rightValue = "";

        if (namesArray.length >= 3) {
            leftValue = namesArray[0];
            middleValue = namesArray[1];
            rightValue = namesArray[2];
        } else if (namesArray.length == 2) {
            leftValue = namesArray[0];
            middleValue = namesArray[1];
        } else if (namesArray.length == 1) {
            leftValue = namesArray[0];
        }
        return Triple.of(leftValue, middleValue, rightValue);
    }

    private void correctResidentsSet(Set<Resident> residents) {
        List<Resident> sortedResidents = new ArrayList<>(residents);
        sortedResidents.sort(Comparator.comparing(Resident::getBedPlace));

        String currentRoom = null;
        List<Resident> removable = new ArrayList<>();

        for (Resident resident : sortedResidents) {
            if (resident.getBedPlace() == 0) {
                removable.add(resident);
            } else if (!resident.getRoomNumber().isEmpty()) {
                currentRoom = resident.getRoomNumber();
            } else if (currentRoom != null) {
                resident.setRoomNumber(currentRoom);
            } else {
                logger.warn("Cannot recognize room number for resident: {}", resident);
            }
        }

        residents.removeAll(removable);
    }

    private String resolveCell(Cell cell) {
        switch (cell.getCellType()) {
            case STRING: {
                return cell.getStringCellValue();
            }
            case NUMERIC: {
                return String.valueOf((short) cell.getNumericCellValue());
            }
            case FORMULA: {
                switch (formulaEvaluator.evaluateFormulaCell(cell)) {
                    case STRING: {
                        return cell.getStringCellValue();
                    }
                    case NUMERIC: {
                        return String.valueOf((short) cell.getNumericCellValue());
                    }
                    case BOOLEAN: {
                        return String.valueOf(cell.getBooleanCellValue());
                    }
                }
            }
            case BLANK: {
                return "";
            }
            case BOOLEAN: {
                return String.valueOf(cell.getBooleanCellValue());
            }
            case ERROR: {
                return String.valueOf(cell.getErrorCellValue());
            }
            default: {
                return "";
            }
        }
    }

    private Object resolveCell(Cell cell, Object defaultValue) {
        if (cell == null) {
            return defaultValue;
        }

        switch (cell.getCellType()) {
            case STRING: {
                return cell.getStringCellValue();
            }
            case NUMERIC: {
                return cell.getNumericCellValue();
            }
            case FORMULA: {
                switch (formulaEvaluator.evaluateFormulaCell(cell)) {
                    case STRING: {
                        return cell.getStringCellValue();
                    }
                    case NUMERIC: {
                        return String.valueOf((short) cell.getNumericCellValue());
                    }
                    case BOOLEAN: {
                        return String.valueOf(cell.getBooleanCellValue());
                    }
                }
            }
            case BLANK: {
                return defaultValue;
            }
            case BOOLEAN: {
                return cell.getBooleanCellValue();
            }
            case ERROR: {
                return cell.getErrorCellValue();
            }
            default: {
                return defaultValue;
            }
        }
    }

    private short parseShortFromCell(Cell cell, String defaultValue) {
        Object resolvedValue = resolveCell(cell, defaultValue);

        if (resolvedValue instanceof String) {
            String stringValue = ((String) resolvedValue).trim();
            if (!stringValue.isEmpty()) {
                try {
                    return (short) Double.parseDouble(stringValue);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number format: " + stringValue, e);
                }
            } else {
                if (defaultValue == null || defaultValue.isEmpty()) {
                    throw new IllegalArgumentException("Default value is empty or null");
                }
                return (short) Double.parseDouble(defaultValue);
            }
        } else if (resolvedValue instanceof Double) {
            return ((Double) resolvedValue).shortValue();
        } else {
            throw new IllegalArgumentException("Unsupported cell type: " + resolvedValue.getClass());
        }
    }

    private Gender resolveGender(String string) {
        Gender gender;
        switch (string) {
            case "Ч", "ч": {
                gender = Gender.MALE;
                break;
            }
            case "Ж", "ж": {
                gender = Gender.FEMALE;
                break;
            }
            default: gender = Gender.NOT_SPECIFIED;
        }
        return gender;
    }

    private boolean cacheContain(String sheetName) {
        AtomicBoolean isContain = new AtomicBoolean(false);
        cache.forEach((key, value) -> {
            if (value.getLeft().equals(sheetName)) isContain.set(true);
        });
        return isContain.get();
    }
}
