package de.windenshelter.flugbuch.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.windenshelter.flugbuch.model.StagingSchleppbetriebEintrag;
import de.windenshelter.flugbuch.repository.SchleppbetriebStagingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Importiert Tagesprotokolle der digitalen Schleppkladde (schleppbetrieb.de).
 *
 * <p>Der CSV-Export ist {@code ;}-separiert, Felder koennen optional in
 * doppelte Anfuehrungszeichen gefasst sein (noetig wenn ein Feld – etwa der
 * Pilotenname "Nachname, Vorname" – selbst ein Komma enthaelt).</p>
 *
 * <p>{@link #importiereAusStream(InputStream)} parst nur (keine DB), so dass
 * die Extraktion isoliert testbar bleibt. {@link #importiereIdempotent(List)}
 * persistiert idempotent ueber {@code external_id} – ein erneuter Import
 * derselben Datei erzeugt keine Duplikate.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchleppbetriebImportService {

    private static final char TRENNZEICHEN = ';';
    private static final String STATUS_PENDING = "PENDING";
    private static final DateTimeFormatter ZEITPUNKT_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // Spaltenreihenfolge des schleppbetrieb.de-Exports:
    // ID;Verein;Zeitpunkt;Piloten Nr;Pilot;Typ;Windenfahrer Nr;Windenfahrer;Startleiter Nr;Startleiter;Winde;Zusatz
    private static final int SPALTE_ID = 0;
    private static final int SPALTE_VEREIN = 1;
    private static final int SPALTE_ZEITPUNKT = 2;
    private static final int SPALTE_PILOT_NR = 3;
    private static final int SPALTE_PILOT = 4;
    private static final int SPALTE_TYP = 5;
    private static final int SPALTE_WINDENFAHRER_NR = 6;
    private static final int SPALTE_WINDENFAHRER = 7;
    private static final int SPALTE_STARTLEITER_NR = 8;
    private static final int SPALTE_STARTLEITER = 9;
    private static final int SPALTE_WINDE = 10;
    private static final int SPALTE_ZUSATZ = 11;
    private static final int SPALTEN_MINIMUM = SPALTE_WINDE + 1;

    private final SchleppbetriebStagingRepository stagingRepository;

    public List<StagingSchleppbetriebEintrag> importiereAusStream(InputStream csvInhalt) {
        Objects.requireNonNull(csvInhalt, "InputStream darf nicht null sein");

        List<StagingSchleppbetriebEintrag> ergebnisListe = new ArrayList<>();

        try (BufferedReader leser = new BufferedReader(
                new InputStreamReader(csvInhalt, StandardCharsets.UTF_8))) {

            String zeile;
            boolean kopfzeileUebersprungen = false;
            int zeilennummer = 0;
            while ((zeile = leser.readLine()) != null) {
                zeilennummer++;
                if (zeile.isBlank()) {
                    continue;
                }
                if (!kopfzeileUebersprungen) {
                    kopfzeileUebersprungen = true;
                    continue;
                }
                ergebnisListe.add(verarbeiteZeile(zeile, zeilennummer));
            }

        } catch (IOException e) {
            log.error("Fehler beim Lesen der Schleppkladde-CSV: {}", e.getMessage());
            throw new SchleppbetriebImportException("Schleppkladde-CSV konnte nicht gelesen werden", e);
        }

        log.info("Schleppkladde-Import: {} Datensaetze extrahiert.", ergebnisListe.size());
        return ergebnisListe;
    }

    @Transactional
    public void importiereIdempotent(List<StagingSchleppbetriebEintrag> eintraege) {
        int gespeichert = 0;
        int uebersprungen = 0;
        for (StagingSchleppbetriebEintrag eintrag : eintraege) {
            if (stagingRepository.existsByExternalId(eintrag.getExternalId())) {
                uebersprungen++;
                continue;
            }
            stagingRepository.save(eintrag);
            gespeichert++;
        }
        log.info("Schleppkladde-Import idempotent: {} gespeichert, {} bereits bekannt.",
                gespeichert, uebersprungen);
    }

    private StagingSchleppbetriebEintrag verarbeiteZeile(String zeile, int zeilennummer) {
        List<String> felder = teileZeile(zeile);
        if (felder.size() < SPALTEN_MINIMUM) {
            throw new SchleppbetriebImportException(String.format(
                    "Zeile %d hat %d Spalten, erwartet werden mindestens %d.",
                    zeilennummer, felder.size(), SPALTEN_MINIMUM));
        }

        return StagingSchleppbetriebEintrag.builder()
                .externalId(ganzzahl(felder, SPALTE_ID, zeilennummer))
                .vereinId(ganzzahl(felder, SPALTE_VEREIN, zeilennummer))
                .zeitpunkt(zeitpunkt(feld(felder, SPALTE_ZEITPUNKT), zeilennummer))
                .pilotNr(ganzzahl(felder, SPALTE_PILOT_NR, zeilennummer))
                .pilot(feld(felder, SPALTE_PILOT))
                .typ(feld(felder, SPALTE_TYP))
                .windenfahrerNr(ganzzahl(felder, SPALTE_WINDENFAHRER_NR, zeilennummer))
                .windenfahrer(feld(felder, SPALTE_WINDENFAHRER))
                .startleiterNr(ganzzahl(felder, SPALTE_STARTLEITER_NR, zeilennummer))
                .startleiter(feld(felder, SPALTE_STARTLEITER))
                .windeName(feld(felder, SPALTE_WINDE))
                .zusatz(feld(felder, SPALTE_ZUSATZ))
                .status(STATUS_PENDING)
                .build();
    }

    /**
     * Zerlegt eine CSV-Zeile entlang {@link #TRENNZEICHEN}. Felder in doppelten
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
            } else if (c == TRENNZEICHEN) {
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

    private Integer ganzzahl(List<String> felder, int index, int zeilennummer) {
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

    private LocalDateTime zeitpunkt(String wert, int zeilennummer) {
        if (wert == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(wert, ZEITPUNKT_FORMAT);
        } catch (DateTimeParseException e) {
            throw new SchleppbetriebImportException(String.format(
                    "Zeile %d: '%s' ist kein gueltiges Datum (erwartet dd.MM.yyyy HH:mm).",
                    zeilennummer, wert), e);
        }
    }
}
