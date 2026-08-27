package de.windenshelter.flugbuch.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

import de.windenshelter.flugbuch.model.StagingSchleppkladdeEintrag;
import de.windenshelter.flugbuch.repository.StagingRepository;

class ExcelImportServiceTest {

    private final StagingRepository repository = mock(StagingRepository.class);
    private final ExcelImportService service = new ExcelImportService(repository);

    @Test
    void importFromStream_throwsException_whenStreamIsNull() {
        assertThatThrownBy(() -> service.importFromStream(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("InputStream");
    }

    @Test
    void importFromStream_throwsException_onCorruptFile() {
        InputStream garbage = new ByteArrayInputStream("not excel".getBytes());

        assertThatThrownBy(() -> service.importFromStream(garbage))
                .isInstanceOf(ExcelImportException.class);
    }

    @Test
    void importFromStream_returnsEmptyList_forEmptySheet() throws Exception {
        byte[] excelBytes = buildEmptyExcel();

        List<StagingSchleppkladdeEintrag> result = service.importFromStream(new ByteArrayInputStream(excelBytes));

        assertThat(result).isEmpty();
    }

    @Test
    void importFromStream_extractsValidRow() throws Exception {
        byte[] excelBytes = buildExcelWithOneDataRow();

        List<StagingSchleppkladdeEintrag> result = service.importFromStream(new ByteArrayInputStream(excelBytes));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKundenNummer()).isEqualTo("12345");
        assertThat(result.get(0).getStatus()).isEqualTo("PENDING");
    }

    @Test
    void importFromStream_skipsRowWithError() throws Exception {
        // One valid row, one with a broken date format
        byte[] excelBytes = buildExcelWithGoodAndBrokenRow();

        List<StagingSchleppkladdeEintrag> result = service.importFromStream(new ByteArrayInputStream(excelBytes));

        // The broken row is skipped, the good one is kept
        assertThat(result).hasSize(1);
    }

    // --- Helper: build minimal in-memory Excel files ---

    private byte[] buildEmptyExcel() throws Exception {
        try (Workbook wb = new XSSFWorkbook();
                var out = new java.io.ByteArrayOutputStream()) {
            wb.createSheet("Test");
            wb.write(out);
            return out.toByteArray();
        }
    }

    private byte[] buildExcelWithOneDataRow() throws Exception {
        try (Workbook wb = new XSSFWorkbook();
                var out = new java.io.ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet();
            // Leave header rows 0-5 empty
            for (int i = 0; i < 6; i++)
                sheet.createRow(i);

            Row dataRow = sheet.createRow(6);
            dataRow.createCell(1).setCellValue(LocalDateTime.now());
            dataRow.createCell(9).setCellValue("12345");
            dataRow.createCell(10).setCellValue("Mustermann, Max");

            wb.write(out);
            return out.toByteArray();
        }
    }

    private byte[] buildExcelWithGoodAndBrokenRow() throws Exception {
        try (Workbook wb = new XSSFWorkbook();
                var out = new java.io.ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet();
            for (int i = 0; i < 6; i++)
                sheet.createRow(i);

            // Good row
            Row good = sheet.createRow(6);
            good.createCell(1).setCellValue(LocalDateTime.now());
            good.createCell(9).setCellValue("11111");
            good.createCell(10).setCellValue("Pilot One");

            // Broken row: date column contains a string instead of a date
            Row broken = sheet.createRow(7);
            broken.createCell(1).setCellValue("not a valid date");
            broken.createCell(9).setCellValue("22222");
            broken.createCell(10).setCellValue("Pilot Two");

            wb.write(out);
            return out.toByteArray();
        }
    }

    @Test
    void importWithOverwrite_doesNothingForEmptyList() {
        service.importWithOverwrite(List.of());
        org.mockito.Mockito.verifyNoInteractions(repository);
    }
}
