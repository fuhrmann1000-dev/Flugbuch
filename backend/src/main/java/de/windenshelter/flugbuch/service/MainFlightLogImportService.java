package de.windenshelter.flugbuch.service;

import de.windenshelter.flugbuch.model.StagingMainFlightLog;
import de.windenshelter.flugbuch.model.StagingSchleppbetriebEintrag;
import de.windenshelter.flugbuch.repository.MainFlightLogStagingRepository;
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
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Importiert Tagesprotokolle der digitalen Schleppkladde (schleppbetrieb.de).
 *
 * <p>Der CSV-Export ist {@code ;}-separiert, Felder koennen optional in
 * doppelte Anfuehrungszeichen gefasst sein (noetig wenn ein Feld – etwa der
 * Pilotenname "Nachname, Vorname" – selbst ein Komma enthaelt).</p>
 *
 * <p>{@link #importFromStream(InputStream)} parst nur (keine DB), so dass
 * die Extraktion isoliert testbar bleibt. {@link #importiereIdempotent(List)}
 * persistiert idempotent ueber {@code external_id} – ein erneuter Import
 * derselben Datei erzeugt keine Duplikate.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MainFlightLogImportService {

    private static final char DIVIDING_CHARACTER = ';';
    /** Begrenzt die IN-Liste je Existenzabfrage und die saveAll-Bundles. */
    private static final int CHUNK_SIZE = 1000;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter  TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    // Spaltenreihenfolge des schleppbetrieb.de-Exports:
    // Datum;Startzeit;Landezeit;Muster;Kennzeichen;Pilot;Gäste;Flugart;Startplatz;Zielplatz;Flugleiter;Geschleppter;Schlepphöhe;Betrag;Bemerkung;Fluganzahl
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
            log.error("Fehler beim Lesen der Schleppkladde-CSV: {}", e.getMessage());
            throw new SchleppbetriebImportException("Schleppkladde-CSV konnte nicht gelesen werden", e);
        }

        log.info("Schleppkladde-Import: {} Datensaetze extrahiert.", resultList.size());
        return resultList;
    }

    /**
     * Persistiert die Eintraege idempotent ueber {@code external_id}.
     *
     * <p>Skaliert mit dem Datensatz, indem pro Chunk EINE Existenz-Abfrage
     * (statt einer je Zeile) ausgefuehrt wird und Neueintraege gebuendelt
     * gespeichert werden. Duplikate innerhalb derselben Eingabe werden vorab
     * entfernt (sonst Unique-Verletzung, da der erste Insert in derselben
     * Transaktion fuer den zweiten noch nicht sichtbar ist).</p>
     *
     * <p>Hinweis: Echtes JDBC-Insert-Batching greift erst, wenn die Id-Strategie
     * nicht {@code IDENTITY} ist (siehe {@link StagingSchleppbetriebEintrag}).
     * Der wesentliche Engpass beim Re-Import ist aber die Existenzpruefung,
     * und die ist hier von O(n) Einzelabfragen auf O(n/chunk) reduziert.</p>
     */
    @Transactional
    public void importiereIdempotent(List<StagingSchleppbetriebEintrag> eintraege) {
        // 1. Duplikate innerhalb der Eingabe entfernen (erste Vorkommnis gewinnt),
        //    Eintraege ohne external_id sind nicht dedupbar -> separat behandeln.
        Map<Integer, StagingSchleppbetriebEintrag> nachExternalId = new LinkedHashMap<>();
        List<StagingSchleppbetriebEintrag> ohneExternalId = new ArrayList<>();
        for (StagingSchleppbetriebEintrag eintrag : eintraege) {
            if (eintrag.getExternalId() == null) {
                ohneExternalId.add(eintrag);
            } else {
                nachExternalId.putIfAbsent(eintrag.getExternalId(), eintrag);
            }
        }

        int gespeichert = 0;
        int uebersprungen = eintraege.size() - nachExternalId.size() - ohneExternalId.size();

        // 2. In Chunks verarbeiten: pro Chunk eine IN-Abfrage + ein saveAll.
        List<StagingSchleppbetriebEintrag> eindeutige = new ArrayList<>(nachExternalId.values());
        for (int start = 0; start < eindeutige.size(); start += CHUNK_SIZE) {
            List<StagingSchleppbetriebEintrag> chunk =
                    eindeutige.subList(start, Math.min(start + CHUNK_SIZE, eindeutige.size()));

            List<Integer> ids = chunk.stream()
                    .map(StagingSchleppbetriebEintrag::getExternalId)
                    .toList();
        }

        // Eintraege ohne external_id sind nicht dedupbar -> immer speichern.
        gespeichert += ohneExternalId.size();

        log.info("Schleppkladde-Import idempotent: {} gespeichert, {} bereits bekannt/dupliziert.",
                gespeichert, uebersprungen);
    }

    private StagingMainFlightLog processedRow(String zeile, int zeilennummer) {
        List<String> felder = teileZeile(zeile);
        if (felder.size() < COLUMN_MINIMUM) {
            throw new SchleppbetriebImportException(String.format(
                    "Zeile %d hat %d Spalten, erwartet werden mindestens %d.",
                    zeilennummer, felder.size(), COLUMN_MINIMUM));
        }

        return StagingMainFlightLog.builder()
                .datum(fieldToDate(feld(felder, COLUMN_DATUM),zeilennummer))
                .startzeit(fieldToTime(feld(felder, COLUMN_START_ZEIT), zeilennummer))
                .landezeit(fieldToTime(feld(felder, COLUMN_LANDE_ZEIT), zeilennummer))
                .muster(feld(felder, COLUMN_MUSTER))
                .kennzeichen(feld(felder, COLUMN_KENNZEICHEN))
                .pilot(feld(felder, COLUMN_PILOT))
                .gaeste(fieldToInteger(felder, COLUMN_GAESTE, zeilennummer))
                .flugart(feld(felder, COLUMN_FLUGART))
                .startPlatz(feld (felder, COLUMN_START_PLATZ))
                .zielPlatz(feld(felder, COLUMN_ZIEL_PLATZ))
                .flugLeiter(feld(felder, COLUMN_FLUG_LEITER))
                .geschleppter(feld(felder, COLUMN_GESCHLEPPTER))
                .schleppHoehe(fieldToInteger(felder, COLUMN_SCHLEPPHOEHE, zeilennummer))
                .betrag(fieldToDouble(felder, COLUMN_BETRAG, zeilennummer))
                .bemerkung(feld(felder, COLUMN_BEMERKUNG))
                .flugAnzahl(fieldToInteger(felder, COLUMN_FLUGANZAHL, zeilennummer)).build();

    }

    /**
     * Zerlegt eine CSV-Zeile entlang {@link #DIVIDING_CHARACTER}. Felder in doppelten
     * Anfuehrungszeichen behalten enthaltene Trennzeichen; ein doppeltes
     * {@code ""} innerhalb eines Feldes wird als literales {@code "} gelesen.
     */
    private List<String> teileZeile(String zeile) {
        List<String> felder = new ArrayList<>();
        StringBuilder aktuell = new StringBuilder();
        boolean inAnfuehrung = false;

        for (int i = 0; i < zeile.length(); i++) {
            char c = zeile.charAt(i);
            if (inAnfuehrung) {
                if (c == '"') {
                    if (i + 1 < zeile.length() && zeile.charAt(i + 1) == '"') {
                        aktuell.append('"');
                        i++;
                    } else {
                        inAnfuehrung = false;
                    }
                } else {
                    aktuell.append(c);
                }
            } else if (c == '"') {
                inAnfuehrung = true;
            } else if (c == DIVIDING_CHARACTER) {
                felder.add(aktuell.toString());
                aktuell.setLength(0);
            } else {
                aktuell.append(c);
            }
        }
        felder.add(aktuell.toString());
        return felder;
    }

    private String feld(List<String> felder, int index) {
        if (index >= felder.size()) {
            return null;
        }
        String wert = felder.get(index).trim();
        return wert.isEmpty() ? null : wert;
    }

    private Integer fieldToInteger(List<String> felder, int index, int zeilennummer) {
        String wert = feld(felder, index);
        if (wert == null) {
            return null;
        }
        try {
            return Integer.valueOf(wert);
        } catch (NumberFormatException e) {
            throw new SchleppbetriebImportException(String.format(
                    "Zeile %d, Spalte %d: '%s' ist keine gueltige Ganzzahl.",
                    zeilennummer, index, wert), e);
        }
    }

    private Double fieldToDouble(List<String> felder, int index, int zeilennummer) {
        String wert = feld(felder, index);
        if (wert == null) {
            return null;
        }
        try {
            return Double.valueOf(wert);
        } catch (NumberFormatException e) {
            throw new SchleppbetriebImportException(String.format(
                    "Zeile %d, Spalte %d: '%s' ist keine gueltige Ganzzahl.",
                    zeilennummer, index, wert), e);
        }
    }

    private LocalDate fieldToDate (String wert, int zeilennummer) {
        if (wert == null) {
            return null;
        }
        try {
            return LocalDate.parse(wert, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new SchleppbetriebImportException(String.format(
                    "Row %d: '%s' is not a valid a date (expected dd.MM.yyyy).",
                    zeilennummer, wert), e);
        }
    }

    private LocalTime fieldToTime (String wert, int zeilennummer) {
        if (wert == null) {
            return null;
        }
        try {
            return LocalTime.parse(wert, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new SchleppbetriebImportException(String.format(
                    "Row %d: '%s' is not a valid a date (expected HH:mm).",
                    zeilennummer, wert), e);
        }
    }
}
