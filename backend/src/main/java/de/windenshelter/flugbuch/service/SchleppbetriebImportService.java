package de.windenshelter.flugbuch.service;

import de.windenshelter.flugbuch.model.StagingSchleppbetriebEintrag;
import de.windenshelter.flugbuch.repository.SchleppbetriebStagingRepository;
import de.windenshelter.flugbuch.service.support.ChunkedDeduplicatingSaver;
import de.windenshelter.flugbuch.service.support.CsvLineParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static de.windenshelter.flugbuch.service.support.CsvLineParser.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchleppbetriebImportService {

    private static final char TRENNZEICHEN = ';';
    private static final String STATUS_PENDING = "PENDING";
    private static final int CHUNK_GROESSE = 1000;
    private static final DateTimeFormatter ZEITPUNKT_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

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
        List<StagingSchleppbetriebEintrag> mitExternalId = eintraege.stream()
                .filter(e -> e.getExternalId() != null)
                .toList();
        List<StagingSchleppbetriebEintrag> ohneExternalId = eintraege.stream()
                .filter(e -> e.getExternalId() == null)
                .toList();

        ChunkedDeduplicatingSaver<StagingSchleppbetriebEintrag, Integer> saver =
                new ChunkedDeduplicatingSaver<>(CHUNK_GROESSE,
                        StagingSchleppbetriebEintrag::getExternalId,
                        chunk -> {
                            List<Integer> ids = chunk.stream()
                                    .map(StagingSchleppbetriebEintrag::getExternalId)
                                    .toList();
                            return new HashSet<>(stagingRepository.findExistingExternalIds(ids));
                        });

        var ergebnis = saver.speichereIdempotent(mitExternalId, stagingRepository::saveAll);

        // Eintraege ohne external_id sind nicht dedupbar -> immer speichern.
        stagingRepository.saveAll(ohneExternalId);

        log.info("Schleppkladde-Import idempotent: {} gespeichert, {} bereits bekannt/dupliziert.",
                ergebnis.gespeichert() + ohneExternalId.size(), ergebnis.uebersprungen());
    }

    private StagingSchleppbetriebEintrag verarbeiteZeile(String zeile, int zeilennummer) {
        List<String> felder = splitLine(zeile, TRENNZEICHEN);
        if (felder.size() < SPALTEN_MINIMUM) {
            throw new SchleppbetriebImportException(String.format(
                    "Zeile %d hat %d Spalten, erwartet werden mindestens %d.",
                    zeilennummer, felder.size(), SPALTEN_MINIMUM));
        }

        return StagingSchleppbetriebEintrag.builder()
                .externalId(parseInteger(felder, SPALTE_ID, zeilennummer))
                .vereinId(parseInteger(felder, SPALTE_VEREIN, zeilennummer))
                .zeitpunkt(parseDateTime(field(felder, SPALTE_ZEITPUNKT), ZEITPUNKT_FORMAT, zeilennummer))
                .pilotNr(parseInteger(felder, SPALTE_PILOT_NR, zeilennummer))
                .pilot(field(felder, SPALTE_PILOT))
                .typ(field(felder, SPALTE_TYP))
                .windenfahrerNr(parseInteger(felder, SPALTE_WINDENFAHRER_NR, zeilennummer))
                .windenfahrer(field(felder, SPALTE_WINDENFAHRER))
                .startleiterNr(parseInteger(felder, SPALTE_STARTLEITER_NR, zeilennummer))
                .startleiter(field(felder, SPALTE_STARTLEITER))
                .windeName(field(felder, SPALTE_WINDE))
                .zusatz(field(felder, SPALTE_ZUSATZ))
                .status(STATUS_PENDING)
                .build();
    }
}