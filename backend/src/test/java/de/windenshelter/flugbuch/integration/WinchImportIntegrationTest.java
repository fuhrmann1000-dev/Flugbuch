package de.windenshelter.flugbuch.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import de.windenshelter.flugbuch.model.StagingWinchkladdeEintrag;
import de.windenshelter.flugbuch.repository.WinchkladdeStagingRepository;
import de.windenshelter.flugbuch.service.WinchImportService;

@Disabled("TDD red - Implementierung folgt in Phase A (next-steps.md)")
@SpringBootTest
@Transactional
class WinchImportIntegrationTest {

    @Autowired
    private WinchImportService winchImportService;

    @Autowired
    private WinchkladdeStagingRepository stagingRepository;

    @Test
    void importiertCsvDateiVollstaendig() {
        InputStream csv = getClass().getClassLoader()
                .getResourceAsStream("test-windenkladde.csv");

        List<StagingWinchkladdeEintrag> extrahiert = winchImportService.importiereAusStream(csv);
        winchImportService.importiereIdempotent(extrahiert);

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
        winchImportService.importiereIdempotent(winchImportService.importiereAusStream(csv1));

        InputStream csv2 = getClass().getClassLoader()
                .getResourceAsStream("test-windenkladde.csv");
        winchImportService.importiereIdempotent(winchImportService.importiereAusStream(csv2));

        assertThat(stagingRepository.count()).isEqualTo(3);
    }
}
