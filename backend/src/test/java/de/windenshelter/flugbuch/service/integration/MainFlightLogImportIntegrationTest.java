package de.windenshelter.flugbuch.service.integration;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

import de.windenshelter.flugbuch.model.StagingMainFlightLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import de.windenshelter.flugbuch.repository.MainFlightLogStagingRepository;
import de.windenshelter.flugbuch.service.MainFlightLogImportService;

@SpringBootTest
@Transactional
class MainFlightLogImportIntegrationTest {

    @Autowired
    private MainFlightLogImportService mainFlightLogImportService;

    @Autowired
    private MainFlightLogStagingRepository stagingRepository;


    @Test
    void repeatedImport_createsNoDuplicates() {
        int countRowsInCsv = 37;
        String csvFileName = "Dezember.csv";

        InputStream csv1 = getClass().getClassLoader()
                .getResourceAsStream(csvFileName);
        mainFlightLogImportService.importIdempotent(this.mainFlightLogImportService.importFromStream(csv1));

        InputStream csv2 = getClass().getClassLoader()
                .getResourceAsStream(csvFileName);
        mainFlightLogImportService.importIdempotent(this.mainFlightLogImportService.importFromStream(csv2));

        assertThat(stagingRepository.count()).isEqualTo(countRowsInCsv);
    }

    @Test
    void importsCsvFile_extractsAllRows() {
        int countRowsInCsv = 37;
        String csvFileName = "Dezember.csv";

        InputStream csv =  getClass().getClassLoader()
                .getResourceAsStream(csvFileName);

        List<StagingMainFlightLog> extracted = mainFlightLogImportService.importFromStream(csv);
        mainFlightLogImportService.importIdempotent(extracted);

        long count = stagingRepository.count();
        assertThat(count).isEqualTo(countRowsInCsv);

        boolean expectedRowExists = stagingRepository.findAll().stream()
                .anyMatch(f -> f.getKennzeichen().equals("D-MIBY")
                && f.getDatum().equals(LocalDate.of(2025, 12, 16))
                && f.getStartzeit().equals(LocalTime.of(9, 30)));

        assertThat(expectedRowExists).isTrue();

    }

    @Test
    void partialAndShuffledReimport_staysIdempotent() {
        int countRowsInCsv = 37;
        int partialSize = 20;
        String csvFileName = "Dezember.csv";

        // Import only a partial subset first
        InputStream csv1 = getClass().getClassLoader()
                .getResourceAsStream(csvFileName);
        List<StagingMainFlightLog> allEntries = mainFlightLogImportService.importFromStream(csv1);

        List<StagingMainFlightLog> partialEntries = new ArrayList<>(allEntries.subList(0, partialSize));
        mainFlightLogImportService.importIdempotent(partialEntries);

        assertThat(stagingRepository.count()).isEqualTo(partialSize);

        //Re-import the full CSV in random order
        InputStream csv2 = getClass().getClassLoader()
                .getResourceAsStream(csvFileName);
        List<StagingMainFlightLog> shuffledEntries = mainFlightLogImportService.importFromStream(csv2);
        Collections.shuffle(shuffledEntries, new Random(42));

        mainFlightLogImportService.importIdempotent(shuffledEntries);

        //Final count must match total rows, no duplicates despite overlap + shuffle
        assertThat(stagingRepository.count()).isEqualTo(countRowsInCsv);

    }
}
