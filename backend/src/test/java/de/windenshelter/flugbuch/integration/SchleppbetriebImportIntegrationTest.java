package de.windenshelter.flugbuch.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import de.windenshelter.flugbuch.model.StagingSchleppbetriebEintrag;
import de.windenshelter.flugbuch.repository.SchleppbetriebStagingRepository;
import de.windenshelter.flugbuch.service.SchleppbetriebImportService;

@SpringBootTest
@Transactional
class SchleppbetriebImportIntegrationTest {

    @Autowired
    private SchleppbetriebImportService schleppbetriebImportService;

    @Autowired
    private SchleppbetriebStagingRepository stagingRepository;

    @Test
    void importiertCsvDateiVollstaendig() {
        InputStream csv = getClass().getClassLoader()
                .getResourceAsStream("test-windenkladde.csv");

        List<StagingSchleppbetriebEintrag> extrahiert = schleppbetriebImportService.importiereAusStream(csv);
        schleppbetriebImportService.importiereIdempotent(extrahiert);

        long anzahl = stagingRepository.count();
        assertThat(anzahl).isEqualTo(3);
        assertThat(stagingRepository.existsByExternalId(198765)).isTrue();
        assertThat(stagingRepository.existsByExternalId(198766)).isTrue();
        assertThat(stagingRepository.existsByExternalId(198767)).isTrue();
    }

    @Test
    void wiederholterImport_erzeugtKeineDuplikate() {
        InputStream csv1 = getClass().getClassLoader()
                .getResourceAsStream("test-windenkladde.csv");
        schleppbetriebImportService.importiereIdempotent(schleppbetriebImportService.importiereAusStream(csv1));

        InputStream csv2 = getClass().getClassLoader()
                .getResourceAsStream("test-windenkladde.csv");
        schleppbetriebImportService.importiereIdempotent(schleppbetriebImportService.importiereAusStream(csv2));

        assertThat(stagingRepository.count()).isEqualTo(3);
    }

    /**
     * Grosser, realitaetsnaher Datensatz: anonymisierter Echt-Export von
     * schleppbetrieb.de (3409 Zeilen, UTF-8-BOM, gequotetes Zeitpunkt-Feld,
     * Umlaute). Stellt sicher, dass Parser und idempotente Persistenz auch
     * mit Produktionsvolumen umgehen.
     */
    @Test
    void importiertGrossenEchtExportUndIstIdempotent() {
        List<StagingSchleppbetriebEintrag> extrahiert = schleppbetriebImportService.importiereAusStream(
                getClass().getClassLoader().getResourceAsStream("anonymized-export-sample.csv"));

        assertThat(extrahiert).hasSize(3409);
        assertThat(extrahiert).allSatisfy(e -> {
            assertThat(e.getExternalId()).isNotNull();
            assertThat(e.getZeitpunkt()).isNotNull();
            assertThat(e.getStatus()).isEqualTo("PENDING");
        });

        schleppbetriebImportService.importiereIdempotent(extrahiert);
        assertThat(stagingRepository.count()).isEqualTo(3409);

        // Zweiter Durchlauf darf keine Duplikate erzeugen (existsByExternalId)
        schleppbetriebImportService.importiereIdempotent(schleppbetriebImportService.importiereAusStream(
                getClass().getClassLoader().getResourceAsStream("anonymized-export-sample.csv")));
        assertThat(stagingRepository.count()).isEqualTo(3409);
    }

    /**
     * Idempotenz auch bei TEIL-ueberlappenden und PERMUTIERTEN Re-Imports:
     * Erst eine Haelfte, dann der ganze (gemischte) Datensatz - am Ende muss
     * jede external_id genau einmal vorhanden sein, unabhaengig von Reihenfolge.
     */
    @Test
    void teilmengeUndPermutation_bleibtIdempotent() {
        List<StagingSchleppbetriebEintrag> alle = schleppbetriebImportService.importiereAusStream(
                getClass().getClassLoader().getResourceAsStream("anonymized-export-sample.csv"));
        assertThat(alle).hasSize(3409);

        // 1. Nur die erste Haelfte importieren
        List<StagingSchleppbetriebEintrag> ersteHaelfte = new ArrayList<>(alle.subList(0, 1700));
        schleppbetriebImportService.importiereIdempotent(ersteHaelfte);
        assertThat(stagingRepository.count()).isEqualTo(1700);

        // 2. Gesamten Datensatz in zufaelliger Reihenfolge nachimportieren
        List<StagingSchleppbetriebEintrag> permutiert = schleppbetriebImportService.importiereAusStream(
                getClass().getClassLoader().getResourceAsStream("anonymized-export-sample.csv"));
        Collections.shuffle(permutiert, new Random(42));
        schleppbetriebImportService.importiereIdempotent(permutiert);

        // Genau die volle Menge, keine Duplikate trotz Ueberlappung + Permutation
        assertThat(stagingRepository.count()).isEqualTo(3409);

        // Jede external_id existiert genau einmal (Stichprobe auf Eindeutigkeit der ersten 50)
        long distinct = permutiert.stream().limit(50)
                .map(StagingSchleppbetriebEintrag::getExternalId)
                .distinct().count();
        assertThat(permutiert.stream().limit(50)).hasSize(50);
        assertThat(distinct).isEqualTo(50);
    }
}
