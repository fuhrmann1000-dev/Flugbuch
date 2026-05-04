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

    private static final int HEADER_ZEILEN = 6;
    private static final int SPALTE_FLUG_DATUM = 1;
    private static final int SPALTE_KUNDEN_NUMMER = 9;
    private static final int SPALTE_PILOTEN_NAME = 10;
    private static final String STATUS_PENDING = "PENDING";

    private final DataFormatter datenFormatter = new DataFormatter();
    private final StagingRepository stagingRepository;

    public List<StagingSchleppkladdeEintrag> importiereAusStream(InputStream dateiInhalt) {
        Objects.requireNonNull(dateiInhalt, "InputStream darf nicht null sein");

        List<StagingSchleppkladdeEintrag> ergebnisListe = new ArrayList<>();

        try (Workbook arbeitsmappe = WorkbookFactory.create(dateiInhalt)) {
            log.info("Excel-Datei erfolgreich geöffnet. Verarbeite Blätter...");
            Sheet blatt = arbeitsmappe.getSheetAt(0);

            for (Row zeile : blatt) {
                if (zeile.getRowNum() < HEADER_ZEILEN || istZeileLeer(zeile)) {
                    continue;
                }
                verarbeiteZeile(zeile, ergebnisListe);
            }

        } catch (java.io.IOException e) {
            log.error("Fehler beim Lesen der Excel-Datei: {}", e.getMessage());
            throw new ExcelImportException("Excel-Datei konnte nicht gelesen werden", e);
        }

        log.info("Import abgeschlossen. {} Datensätze extrahiert.", ergebnisListe.size());
        return ergebnisListe;
    }

    @Transactional
    public void importiereMitUeberschreiben(List<StagingSchleppkladdeEintrag> neueEintraege) {
        for (StagingSchleppkladdeEintrag eintrag : neueEintraege) {
            log.debug("Prüfe auf existierende Daten für: {} / {}",
                    eintrag.getFlugDatum(), eintrag.getKundenNummer());

            stagingRepository.deleteByFlugDatumAndKundenNummer(
                    eintrag.getFlugDatum(), eintrag.getKundenNummer());
            stagingRepository.save(eintrag);
        }
        log.info("{} Einträge im Staging verarbeitet (Überschreiben aktiv).",
                neueEintraege.size());
    }

    private StagingSchleppkladdeEintrag mappeZeileZuEntity(Row zeile) {
        return StagingSchleppkladdeEintrag.builder()
                .flugDatum(zeile.getCell(SPALTE_FLUG_DATUM).getLocalDateTimeCellValue())
                .kundenNummer(datenFormatter.formatCellValue(zeile.getCell(SPALTE_KUNDEN_NUMMER)))
                .nameDesPiloten(leseStringZelle(zeile.getCell(SPALTE_PILOTEN_NAME)))
                .status(STATUS_PENDING)
                .build();
    }

    private String leseStringZelle(Cell zelle) {
        if (zelle == null) {
            return "";
        }
        return datenFormatter.formatCellValue(zelle);
    }

    private boolean istZeileLeer(Row zeile) {
        Cell datumZelle = zeile.getCell(SPALTE_FLUG_DATUM);
        return datumZelle == null || datumZelle.getCellType() == CellType.BLANK;
    }

    private void verarbeiteZeile(Row zeile, List<StagingSchleppkladdeEintrag> ergebnisListe) {
        try {
            ergebnisListe.add(mappeZeileZuEntity(zeile));
        } catch (Exception e) {
            log.warn("Fehler beim Verarbeiten von Zeile {}: {}",
                    zeile.getRowNum(), e.getMessage());
        }
    }
}