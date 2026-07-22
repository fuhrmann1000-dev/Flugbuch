package de.windenshelter.flugbuch.integration;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
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
}
