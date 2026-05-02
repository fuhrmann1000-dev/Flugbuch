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
    void importiereAusStream_wirftException_wennStreamNull() {
        assertThatThrownBy(() -> service.importiereAusStream(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("InputStream");
    }

    @Test
    void importiereAusStream_wirftException_beiKorrupterDatei() {
        InputStream muell = new ByteArrayInputStream("kein excel".getBytes());

        assertThatThrownBy(() -> service.importiereAusStream(muell))
                .isInstanceOf(ExcelImportException.class);
    }

    @Test
    void importiereAusStream_gibtLeereListeBeiLeeremSheet() throws Exception {
        byte[] excelBytes = erzeugeLeeresExcel();

        List<StagingSchleppkladdeEintrag> ergebnis = service.importiereAusStream(new ByteArrayInputStream(excelBytes));

        assertThat(ergebnis).isEmpty();
    }

    @Test
    void importiereAusStream_extrahiertGueltigeZeile() throws Exception {
        byte[] excelBytes = erzeugeExcelMitEinerDatenZeile();

        List<StagingSchleppkladdeEintrag> ergebnis = service.importiereAusStream(new ByteArrayInputStream(excelBytes));

        assertThat(ergebnis).hasSize(1);
        assertThat(ergebnis.get(0).getKundenNummer()).isEqualTo("12345");
        assertThat(ergebnis.get(0).getStatus()).isEqualTo("PENDING");
    }

    @Test
    void importiereAusStream_ueberspringtZeileMitFehler() throws Exception {
        // Eine Zeile gültig, eine mit kaputtem Datums-Format
        byte[] excelBytes = erzeugeExcelMitGuteUndKaputteZeile();

        List<StagingSchleppkladdeEintrag> ergebnis = service.importiereAusStream(new ByteArrayInputStream(excelBytes));

        // Die kaputte Zeile wird übersprungen, die gute aufgenommen
        assertThat(ergebnis).hasSize(1);
    }

    // --- Helper: minimale Excel-Files in Memory bauen ---

    private byte[] erzeugeLeeresExcel() throws Exception {
        try (Workbook wb = new XSSFWorkbook();
                var out = new java.io.ByteArrayOutputStream()) {
            wb.createSheet("Test");
            wb.write(out);
            return out.toByteArray();
        }
    }

    private byte[] erzeugeExcelMitEinerDatenZeile() throws Exception {
        try (Workbook wb = new XSSFWorkbook();
                var out = new java.io.ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet();
            // Header-Zeilen 0-5 leer lassen
            for (int i = 0; i < 6; i++)
                sheet.createRow(i);

            Row datenZeile = sheet.createRow(6);
            datenZeile.createCell(1).setCellValue(LocalDateTime.now());
            datenZeile.createCell(9).setCellValue("12345");
            datenZeile.createCell(10).setCellValue("Mustermann, Max");

            wb.write(out);
            return out.toByteArray();
        }
    }

    private byte[] erzeugeExcelMitGuteUndKaputteZeile() throws Exception {
        try (Workbook wb = new XSSFWorkbook();
                var out = new java.io.ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet();
            for (int i = 0; i < 6; i++)
                sheet.createRow(i);

            // Gute Zeile
            Row gut = sheet.createRow(6);
            gut.createCell(1).setCellValue(LocalDateTime.now());
            gut.createCell(9).setCellValue("11111");
            gut.createCell(10).setCellValue("Pilot Eins");

            // Kaputte Zeile: Datums-Spalte enthält String statt Datum
            Row kaputt = sheet.createRow(7);
            kaputt.createCell(1).setCellValue("kein gültiges Datum");
            kaputt.createCell(9).setCellValue("22222");
            kaputt.createCell(10).setCellValue("Pilot Zwei");

            wb.write(out);
            return out.toByteArray();
        }
    }

    @Test
    void importiereMitUeberschreiben_machtNichtsBeiLeererListe() {
        service.importiereMitUeberschreiben(List.of());
        org.mockito.Mockito.verifyNoInteractions(repository);
    }
}