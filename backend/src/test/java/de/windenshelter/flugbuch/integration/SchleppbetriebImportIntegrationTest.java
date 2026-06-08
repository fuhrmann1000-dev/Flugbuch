package de.windenshelter.flugbuch.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.List;

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
}
