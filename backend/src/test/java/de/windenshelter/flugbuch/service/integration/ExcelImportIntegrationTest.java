package de.windenshelter.flugbuch.service.integration;

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
@Transactional // Rollback after every test, for clean coverage runs
class ExcelImportIntegrationTest {

        @Autowired
        private ExcelImportService excelImportService;

        @Autowired
        private StagingRepository stagingRepository;

        @Test
        void fullImportIntoDatabase_succeeds() {
                // Given
                InputStream testFile = getClass().getClassLoader()
                                .getResourceAsStream("test-schleppkladde.xlsx");

                // When
                List<StagingSchleppkladdeEintrag> extracted = excelImportService.importFromStream(testFile);
                stagingRepository.saveAll(extracted);

                // Then
                long countInDatabase = stagingRepository.count();
                assertThat(countInDatabase).isGreaterThan(0);

                // Spot-check the business key (date + customer number)
                boolean found = stagingRepository.findAll().stream()
                                .anyMatch(e -> e.getKundenNummer().equals("2025031020"));
                assertThat(found).isTrue();
        }

        @Test
        void overwriteWorks() {
                // 1. Save an initial entry
                StagingSchleppkladdeEintrag oldEntry = StagingSchleppkladdeEintrag.builder()
                                .flugDatum(LocalDateTime.of(2025, 5, 1, 12, 0))
                                .kundenNummer("2025031020")
                                .nameDesPiloten("Wrong Name")
                                .build();
                stagingRepository.save(oldEntry);

                // 2. Import a new entry with the same key but a different name
                StagingSchleppkladdeEintrag newEntry = StagingSchleppkladdeEintrag.builder()
                                .flugDatum(LocalDateTime.of(2025, 5, 1, 12, 0))
                                .kundenNummer("2025031020")
                                .nameDesPiloten("Yousefi, Faroogh") // Corrected name
                                .build();

                excelImportService.importWithOverwrite(List.of(newEntry));

                // 3. Verify
                List<StagingSchleppkladdeEintrag> result = stagingRepository.findAll();
                assertEquals(1, result.size(), "Only one entry should exist");
                assertEquals("Yousefi, Faroogh", result.get(0).getNameDesPiloten(),
                                "The name should have been overwritten");
        }

}
