package de.windenshelter.flugbuch.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.windenshelter.flugbuch.model.StagingSchleppkladdeEintrag;
import de.windenshelter.flugbuch.repository.StagingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private static final int HEADER_ROWS = 6;
    private static final int COLUMN_FLIGHT_DATE = 1;
    private static final int COLUMN_CUSTOMER_NUMBER = 9;
    private static final int COLUMN_PILOT_NAME = 10;
    private static final String STATUS_PENDING = "PENDING";

    private final DataFormatter dataFormatter = new DataFormatter();
    private final StagingRepository stagingRepository;

    public List<StagingSchleppkladdeEintrag> importFromStream(InputStream fileContent) {
        Objects.requireNonNull(fileContent, "InputStream must not be null");

        List<StagingSchleppkladdeEintrag> resultList = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(fileContent)) {
            log.info("Excel file opened successfully. Processing sheets...");
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() < HEADER_ROWS || isRowEmpty(row)) {
                    continue;
                }
                processRow(row, resultList);
            }

        } catch (java.io.IOException e) {
            log.error("Error reading Excel file: {}", e.getMessage());
            throw new ExcelImportException("Excel file could not be read", e);
        }

        log.info("Import complete: {} records extracted.", resultList.size());
        return resultList;
    }

    @Transactional
    public void importWithOverwrite(List<StagingSchleppkladdeEintrag> newEntries) {
        for (StagingSchleppkladdeEintrag entry : newEntries) {
            log.debug("Checking for existing data for: {} / {}",
                    entry.getFlugDatum(), entry.getKundenNummer());

            stagingRepository.deleteByFlugDatumAndKundenNummer(
                    entry.getFlugDatum(), entry.getKundenNummer());
            stagingRepository.save(entry);
        }
        log.info("{} entries processed in staging (overwrite enabled).",
                newEntries.size());
    }

    private StagingSchleppkladdeEintrag mapRowToEntity(Row row) {
        return StagingSchleppkladdeEintrag.builder()
                .flugDatum(row.getCell(COLUMN_FLIGHT_DATE).getLocalDateTimeCellValue())
                .kundenNummer(dataFormatter.formatCellValue(row.getCell(COLUMN_CUSTOMER_NUMBER)))
                .nameDesPiloten(readStringCell(row.getCell(COLUMN_PILOT_NAME)))
                .status(STATUS_PENDING)
                .build();
    }

    private String readStringCell(Cell cell) {
        if (cell == null) {
            return "";
        }
        return dataFormatter.formatCellValue(cell);
    }

    private boolean isRowEmpty(Row row) {
        Cell dateCell = row.getCell(COLUMN_FLIGHT_DATE);
        return dateCell == null || dateCell.getCellType() == CellType.BLANK;
    }

    private void processRow(Row row, List<StagingSchleppkladdeEintrag> resultList) {
        try {
            resultList.add(mapRowToEntity(row));
        } catch (Exception e) {
            log.warn("Error processing row {}: {}",
                    row.getRowNum(), e.getMessage());
        }
    }
}
