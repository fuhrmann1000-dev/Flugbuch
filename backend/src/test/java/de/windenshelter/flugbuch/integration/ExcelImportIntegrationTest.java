package de.windenshelter.flugbuch.integration;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import de.windenshelter.flugbuch.model.StagingSchleppkladdeEintrag;
import de.windenshelter.flugbuch.repository.StagingRepository;
import de.windenshelter.flugbuch.service.ExcelImportService;

@SpringBootTest
@Transactional // Rollback nach jedem Test für saubere Coverage-Läufe
class ExcelImportIntegrationTest {

        @Autowired
        private ExcelImportService excelImportService;

        @Autowired
        private StagingRepository stagingRepository;

        @Test
        void testVollstaendigerImportInDatenbank() {
                // Given
                InputStream testDatei = getClass().getClassLoader()
                                .getResourceAsStream("test-schleppkladde.xlsx");

                // When
                List<StagingSchleppkladdeEintrag> extrahiert = excelImportService.importiereAusStream(testDatei);
                stagingRepository.saveAll(extrahiert);

                // Then
                long anzahlInDatenbank = stagingRepository.count();
                assertThat(anzahlInDatenbank).isGreaterThan(0);

                // Stichprobe Business-Key (Datum + Kundennummer)
                boolean gefunden = stagingRepository.findAll().stream()
                                .anyMatch(e -> e.getKundenNummer().equals("2025031020"));
                assertThat(gefunden).isTrue();
        }

        @Test
        void testUeberschreibenFunktioniert() {
                // 1. Ersten Eintrag speichern
                StagingSchleppkladdeEintrag alterEintrag = StagingSchleppkladdeEintrag.builder()
                                .flugDatum(LocalDateTime.of(2025, 5, 1, 12, 0))
                                .kundenNummer("2025031020")
                                .nameDesPiloten("Falscher Name")
                                .build();
                stagingRepository.save(alterEintrag);

                // 2. Neuen Eintrag mit gleichem Key aber anderem Namen importieren
                StagingSchleppkladdeEintrag neuerEintrag = StagingSchleppkladdeEintrag.builder()
                                .flugDatum(LocalDateTime.of(2025, 5, 1, 12, 0))
                                .kundenNummer("2025031020")
                                .nameDesPiloten("Yousefi, Faroogh") // Korrigierter Name
                                .build();

                excelImportService.importiereMitUeberschreiben(List.of(neuerEintrag));

                // 3. Verifizieren
                List<StagingSchleppkladdeEintrag> result = stagingRepository.findAll();
                assertEquals(1, result.size(), "Es sollte nur ein Eintrag existieren");
                assertEquals("Yousefi, Faroogh", result.get(0).getNameDesPiloten(),
                                "Der Name sollte überschrieben sein");
        }

}