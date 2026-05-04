package de.windenshelter.flugbuch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.windenshelter.flugbuch.model.StagingWinchkladdeEintrag;
import de.windenshelter.flugbuch.repository.WinchkladdeStagingRepository;

/**
 * TDD red - Implementierung in Folge-PR. Tests sind disabled, damit
 * die CI gruen bleibt; werden waehrend der Implementierung
 * Schritt fuer Schritt aktiviert.
 */
@Disabled("TDD red - Implementierung folgt in Phase A (next-steps.md)")
class WinchImportServiceTest {

    private final WinchkladdeStagingRepository repository = mock(WinchkladdeStagingRepository.class);
    private final WinchImportService service = new WinchImportService(repository);

    private static final String HEADER =
            "ID;Verein;Zeitpunkt;\"Piloten Nr\";Pilot;Typ;\"Windenfahrer Nr\";Windenfahrer;\"Startleiter Nr\";Startleiter;Winde;Zusatz";

    @Test
    void importiereAusStream_extrahiertEineGueltigeZeile() {
        String csv = HEADER + "\n"
                + "198765;12;01.05.2026 10:15;3421;\"Mustermann, Max\";GS Solo;512;\"Schmidt, Anna\";207;\"Weber, Tom\";Felix 1;\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingWinchkladdeEintrag> result = service.importiereAusStream(stream);

        assertThat(result).hasSize(1);
        StagingWinchkladdeEintrag eintrag = result.get(0);
        assertThat(eintrag.getExternalId()).isEqualTo(198765);
        assertThat(eintrag.getVereinId()).isEqualTo(12);
        assertThat(eintrag.getZeitpunkt()).isEqualTo(LocalDateTime.of(2026, 5, 1, 10, 15));
        assertThat(eintrag.getPilotNr()).isEqualTo(3421);
        assertThat(eintrag.getPilot()).isEqualTo("Mustermann, Max");
        assertThat(eintrag.getWindeName()).isEqualTo("Felix 1");
        assertThat(eintrag.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void importiereAusStream_ueberspringtHeader() {
        String csv = HEADER + "\n"
                + "198765;12;01.05.2026 10:15;3421;\"Mustermann, Max\";GS Solo;512;\"Schmidt, Anna\";207;\"Weber, Tom\";Felix 1;\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingWinchkladdeEintrag> result = service.importiereAusStream(stream);

        assertThat(result).hasSize(1);
    }

    @Test
    void importiereAusStream_parseDeutschesDatumsformat() {
        String csv = HEADER + "\n"
                + "198765;12;15.07.2025 18:42;3421;Pilot;Solo;512;Winde;207;Leiter;Felix 1;\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingWinchkladdeEintrag> result = service.importiereAusStream(stream);

        assertThat(result.get(0).getZeitpunkt()).isEqualTo(LocalDateTime.of(2025, 7, 15, 18, 42));
    }

    @Test
    void importiereAusStream_wirftBeiNullStream() {
        assertThatThrownBy(() -> service.importiereAusStream(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void importiereIdempotent_speichertNeueEintraege() {
        StagingWinchkladdeEintrag neu = StagingWinchkladdeEintrag.builder()
                .externalId(198765)
                .build();
        when(repository.existsByExternalId(198765)).thenReturn(false);

        service.importiereIdempotent(List.of(neu));

        org.mockito.Mockito.verify(repository).save(neu);
    }

    @Test
    void importiereIdempotent_ueberspringtBekannteExternalId() {
        StagingWinchkladdeEintrag bekannt = StagingWinchkladdeEintrag.builder()
                .externalId(198765)
                .build();
        when(repository.existsByExternalId(198765)).thenReturn(true);

        service.importiereIdempotent(List.of(bekannt));

        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).save(bekannt);
    }
}
