package de.windenshelter.flugbuch.service;

import de.windenshelter.flugbuch.model.StagingMainFlightLog;
import de.windenshelter.flugbuch.repository.MainFlightLogStagingRepository;
import de.windenshelter.flugbuch.service.support.ChunkedDeduplicatingSaver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static de.windenshelter.flugbuch.service.support.CsvLineParser.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainFlightLogImportService {

    private static final char DIVIDING_CHARACTER = ';';
    private static final int CHUNK_SIZE = 1000;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final int COLUMN_DATUM = 0;
    private static final int COLUMN_START_ZEIT = 1;
    private static final int COLUMN_LANDE_ZEIT = 2;
    private static final int COLUMN_MUSTER = 3;
    private static final int COLUMN_KENNZEICHEN = 4;
    private static final int COLUMN_PILOT = 5;
    private static final int COLUMN_GAESTE = 6;
    private static final int COLUMN_FLUGART = 7;
    private static final int COLUMN_START_PLATZ = 8;
    private static final int COLUMN_ZIEL_PLATZ = 9;
    private static final int COLUMN_FLUG_LEITER = 10;
    private static final int COLUMN_GESCHLEPPTER = 11;
    private static final int COLUMN_SCHLEPPHOEHE = 12;
    private static final int COLUMN_BETRAG = 13;
    private static final int COLUMN_BEMERKUNG = 14;
    private static final int COLUMN_FLUGANZAHL = 15;
    private static final int COLUMN_MINIMUM = 16;

    private final MainFlightLogStagingRepository stagingRepository;

    public List<StagingMainFlightLog> importFromStream(InputStream csvContent) {
        Objects.requireNonNull(csvContent, "InputStream must not be null");

        List<StagingMainFlightLog> resultList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(csvContent, StandardCharsets.UTF_8))) {

            String line;
            boolean headlineJumpedOver = false;
            int rowNumber = 0;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.isBlank()) {
                    continue;
                }
                if (!headlineJumpedOver) {
                    headlineJumpedOver = true;
                    continue;
                }
                resultList.add(processedRow(line, rowNumber));
            }

        } catch (IOException e) {
            log.error("Fehler beim Lesen der Hauptflugbuch-CSV: {}", e.getMessage());
            throw new SchleppbetriebImportException("Hauptflugbuch-CSV konnte nicht gelesen werden", e);
        }

        log.info("Hauptflugbuch-Import: {} Datensaetze extrahiert.", resultList.size());
        return resultList;
    }

    @Transactional
    public void importIdempotent(List<StagingMainFlightLog> eintraege) {
        ChunkedDeduplicatingSaver<StagingMainFlightLog, NaturalKey> saver =
                new ChunkedDeduplicatingSaver<>(CHUNK_SIZE, NaturalKey::of, this::sucheBekannteSchluessel);

        var ergebnis = saver.speichereIdempotent(eintraege, stagingRepository::saveAll);

        log.info("Hauptflugbuch-Import idempotent: {} gespeichert, {} bereits bekannt/dupliziert.",
                ergebnis.gespeichert(), ergebnis.uebersprungen());
    }

    private Set<NaturalKey> sucheBekannteSchluessel(List<StagingMainFlightLog> chunk) {
        Set<String> kennzeichenSet = chunk.stream()
                .map(StagingMainFlightLog::getKennzeichen)
                .collect(Collectors.toSet());
        Set<LocalDate> datumSet = chunk.stream()
                .map(StagingMainFlightLog::getDatum)
                .collect(Collectors.toSet());

        return stagingRepository.findByKennzeichenInAndDatumIn(kennzeichenSet, datumSet).stream()
                .map(NaturalKey::of)
                .collect(Collectors.toSet());
    }

    /** Natuerlicher Schluessel fuer Duplikaterkennung, da die CSV keine externalId liefert. */
    private record NaturalKey(LocalDate datum, LocalTime startzeit, String kennzeichen) {
        static NaturalKey of(StagingMainFlightLog eintrag) {
            return new NaturalKey(eintrag.getDatum(), eintrag.getStartzeit(), eintrag.getKennzeichen());
        }
    }

    private StagingMainFlightLog processedRow(String zeile, int zeilennummer) {
        List<String> felder = splitLine(zeile, DIVIDING_CHARACTER);
        if (felder.size() < COLUMN_MINIMUM) {
            throw new SchleppbetriebImportException(String.format(
                    "Zeile %d hat %d Spalten, erwartet werden mindestens %d.",
                    zeilennummer, felder.size(), COLUMN_MINIMUM));
        }

        return StagingMainFlightLog.builder()
                .datum(parseDate(field(felder, COLUMN_DATUM), DATE_FORMAT, zeilennummer))
                .startzeit(parseTime(field(felder, COLUMN_START_ZEIT), TIME_FORMAT, zeilennummer))
                .landezeit(parseTime(field(felder, COLUMN_LANDE_ZEIT), TIME_FORMAT, zeilennummer))
                .muster(field(felder, COLUMN_MUSTER))
                .kennzeichen(field(felder, COLUMN_KENNZEICHEN))
                .pilot(field(felder, COLUMN_PILOT))
                .gaeste(parseInteger(felder, COLUMN_GAESTE, zeilennummer))
                .flugart(field(felder, COLUMN_FLUGART))
                .startPlatz(field(felder, COLUMN_START_PLATZ))
                .zielPlatz(field(felder, COLUMN_ZIEL_PLATZ))
                .flugLeiter(field(felder, COLUMN_FLUG_LEITER))
                .geschleppter(field(felder, COLUMN_GESCHLEPPTER))
                .schleppHoehe(parseInteger(felder, COLUMN_SCHLEPPHOEHE, zeilennummer))
                .betrag(parseDouble(felder, COLUMN_BETRAG, zeilennummer))
                .bemerkung(field(felder, COLUMN_BEMERKUNG))
                .flugAnzahl(parseInteger(felder, COLUMN_FLUGANZAHL, zeilennummer))
                .build();
    }
}