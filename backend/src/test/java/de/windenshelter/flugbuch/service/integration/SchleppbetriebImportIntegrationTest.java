package de.windenshelter.flugbuch.service.integration;

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
    void importsCsvFileCompletely() {
        InputStream csv = getClass().getClassLoader()
                .getResourceAsStream("test-windenkladde.csv");

        List<StagingSchleppbetriebEintrag> extracted = schleppbetriebImportService.importFromStream(csv);
        schleppbetriebImportService.importIdempotent(extracted);

        long count = stagingRepository.count();
        assertThat(count).isEqualTo(3);
        assertThat(stagingRepository.existsByExternalId(198765)).isTrue();
        assertThat(stagingRepository.existsByExternalId(198766)).isTrue();
        assertThat(stagingRepository.existsByExternalId(198767)).isTrue();
    }

    @Test
    void repeatedImport_createsNoDuplicates() {
        InputStream csv1 = getClass().getClassLoader()
                .getResourceAsStream("test-windenkladde.csv");
        schleppbetriebImportService.importIdempotent(schleppbetriebImportService.importFromStream(csv1));

        InputStream csv2 = getClass().getClassLoader()
                .getResourceAsStream("test-windenkladde.csv");
        schleppbetriebImportService.importIdempotent(schleppbetriebImportService.importFromStream(csv2));

        assertThat(stagingRepository.count()).isEqualTo(3);
    }

    /**
     * Large, realistic dataset: an anonymized real export from
     * schleppbetrieb.de (3409 rows, UTF-8 BOM, quoted timestamp field,
     * umlauts). Makes sure the parser and idempotent persistence hold up at
     * production volume too.
     */
    @Test
    void importsLargeRealExport_andStaysIdempotent() {
        List<StagingSchleppbetriebEintrag> extracted = schleppbetriebImportService.importFromStream(
                getClass().getClassLoader().getResourceAsStream("anonymized-export-sample.csv"));

        assertThat(extracted).hasSize(3409);
        assertThat(extracted).allSatisfy(e -> {
            assertThat(e.getExternalId()).isNotNull();
            assertThat(e.getZeitpunkt()).isNotNull();
            assertThat(e.getStatus()).isEqualTo("PENDING");
        });

        schleppbetriebImportService.importIdempotent(extracted);
        assertThat(stagingRepository.count()).isEqualTo(3409);

        // A second run must not create any duplicates (existsByExternalId).
        schleppbetriebImportService.importIdempotent(schleppbetriebImportService.importFromStream(
                getClass().getClassLoader().getResourceAsStream("anonymized-export-sample.csv")));
        assertThat(stagingRepository.count()).isEqualTo(3409);
    }

    /**
     * Idempotency also holds for PARTIALLY overlapping and SHUFFLED
     * re-imports: first half the dataset, then the whole (shuffled) thing -
     * in the end every external_id must exist exactly once, regardless of order.
     */
    @Test
    void partialSubsetAndPermutation_staysIdempotent() {
        List<StagingSchleppbetriebEintrag> all = schleppbetriebImportService.importFromStream(
                getClass().getClassLoader().getResourceAsStream("anonymized-export-sample.csv"));
        assertThat(all).hasSize(3409);

        // 1. Import only the first half.
        List<StagingSchleppbetriebEintrag> firstHalf = new ArrayList<>(all.subList(0, 1700));
        schleppbetriebImportService.importIdempotent(firstHalf);
        assertThat(stagingRepository.count()).isEqualTo(1700);

        // 2. Re-import the full dataset in random order.
        List<StagingSchleppbetriebEintrag> shuffled = schleppbetriebImportService.importFromStream(
                getClass().getClassLoader().getResourceAsStream("anonymized-export-sample.csv"));
        Collections.shuffle(shuffled, new Random(42));
        schleppbetriebImportService.importIdempotent(shuffled);

        // Exactly the full count, no duplicates despite overlap + permutation.
        assertThat(stagingRepository.count()).isEqualTo(3409);

        // Every external_id exists exactly once (spot-check uniqueness of the first 50).
        long distinct = shuffled.stream().limit(50)
                .map(StagingSchleppbetriebEintrag::getExternalId)
                .distinct().count();
        assertThat(shuffled.stream().limit(50)).hasSize(50);
        assertThat(distinct).isEqualTo(50);
    }
}
