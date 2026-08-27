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

import org.junit.jupiter.api.Test;

import de.windenshelter.flugbuch.model.StagingSchleppbetriebEintrag;
import de.windenshelter.flugbuch.repository.SchleppbetriebStagingRepository;

class SchleppbetriebImportServiceTest {

    private final SchleppbetriebStagingRepository repository = mock(SchleppbetriebStagingRepository.class);
    private final SchleppbetriebImportService service = new SchleppbetriebImportService(repository);

    // Header text matches the real schleppbetrieb.de CSV export format - left
    // in German since that's the actual external file format being parsed.
    private static final String HEADER =
            "ID;Verein;Zeitpunkt;\"Piloten Nr\";Pilot;Typ;\"Windenfahrer Nr\";Windenfahrer;\"Startleiter Nr\";Startleiter;Winde;Zusatz";

    @Test
    void importFromStream_extractsOneValidRow() {
        String csv = HEADER + "\n"
                + "198765;12;01.05.2026 10:15;3421;\"Mustermann, Max\";GS Solo;512;\"Schmidt, Anna\";207;\"Weber, Tom\";Felix 1;\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingSchleppbetriebEintrag> result = service.importFromStream(stream);

        assertThat(result).hasSize(1);
        StagingSchleppbetriebEintrag entry = result.get(0);
        assertThat(entry.getExternalId()).isEqualTo(198765);
        assertThat(entry.getVereinId()).isEqualTo(12);
        assertThat(entry.getZeitpunkt()).isEqualTo(LocalDateTime.of(2026, 5, 1, 10, 15));
        assertThat(entry.getPilotNr()).isEqualTo(3421);
        assertThat(entry.getPilot()).isEqualTo("Mustermann, Max");
        assertThat(entry.getWindeName()).isEqualTo("Felix 1");
        assertThat(entry.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void importFromStream_skipsHeader() {
        String csv = HEADER + "\n"
                + "198765;12;01.05.2026 10:15;3421;\"Mustermann, Max\";GS Solo;512;\"Schmidt, Anna\";207;\"Weber, Tom\";Felix 1;\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingSchleppbetriebEintrag> result = service.importFromStream(stream);

        assertThat(result).hasSize(1);
    }

    @Test
    void importFromStream_parsesGermanDateFormat() {
        String csv = HEADER + "\n"
                + "198765;12;15.07.2025 18:42;3421;Pilot;Solo;512;Winde;207;Leiter;Felix 1;\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingSchleppbetriebEintrag> result = service.importFromStream(stream);

        assertThat(result.get(0).getZeitpunkt()).isEqualTo(LocalDateTime.of(2025, 7, 15, 18, 42));
    }

    @Test
    void importFromStream_parsesQuotedTimestamp() {
        // The real schleppbetrieb.de export wraps the timestamp field in quotes.
        String csv = HEADER + "\n"
                + "17590;12;\"18.04.2026 14:46\";1057;\"Bernd Mueller\";\"GS Solo\";90;\"Ralf Stein\";498;\"Maja Goetz\";\"Felix 1\";\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingSchleppbetriebEintrag> result = service.importFromStream(stream);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getZeitpunkt()).isEqualTo(LocalDateTime.of(2026, 4, 18, 14, 46));
        assertThat(result.get(0).getWindeName()).isEqualTo("Felix 1");
    }

    @Test
    void importFromStream_toleratesUtf8Bom() {
        // The real export starts with a UTF-8 BOM (EF BB BF) before the header row.
        String csv = "﻿" + HEADER + "\n"
                + "17590;12;\"18.04.2026 14:46\";1057;\"Bernd Mueller\";\"GS Solo\";90;\"Ralf Stein\";498;\"Maja Goetz\";\"Felix 1\";\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingSchleppbetriebEintrag> result = service.importFromStream(stream);

        // The BOM sits on the header row, which gets skipped - the data row stays clean.
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExternalId()).isEqualTo(17590);
    }

    @Test
    void importFromStream_preservesUmlautsInNames() {
        String csv = HEADER + "\n"
                + "17587;12;\"18.04.2026 14:37\";634;\"Gesa Schütze\";\"GS Solo\";107;\"Uwe Müller\";498;\"Maja Götz\";\"GSW 1\";\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingSchleppbetriebEintrag> result = service.importFromStream(stream);

        assertThat(result.get(0).getPilot()).isEqualTo("Gesa Schütze");
        assertThat(result.get(0).getWindenfahrer()).isEqualTo("Uwe Müller");
    }

    @Test
    void importFromStream_preservesInternationalNames() {
        // UTF-8 must survive end-to-end: Arabic, Chinese, French, Cyrillic.
        String csv = HEADER + "\n"
                + "1;12;\"01.05.2026 10:00\";1;\"محمد علي\";Solo;2;\"李伟\";3;\"François Léveillé\";Felix 1;\n"
                + "2;12;\"01.05.2026 10:05\";4;\"Иван Петров\";Solo;2;\"Müller-Œuvre\";3;\"Łukasz Brzęczyszczykiewicz\";Felix 1;\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingSchleppbetriebEintrag> result = service.importFromStream(stream);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPilot()).isEqualTo("محمد علي");        // Arabic
        assertThat(result.get(0).getWindenfahrer()).isEqualTo("李伟");      // Chinese
        assertThat(result.get(0).getStartleiter()).isEqualTo("François Léveillé"); // French
        assertThat(result.get(1).getPilot()).isEqualTo("Иван Петров");     // Cyrillic
        assertThat(result.get(1).getStartleiter()).isEqualTo("Łukasz Brzęczyszczykiewicz");
    }

    @Test
    void importIdempotent_removesDuplicatesWithinTheInput() {
        // Same external_id twice in the same input -> saved only once.
        StagingSchleppbetriebEintrag a = StagingSchleppbetriebEintrag.builder().externalId(42).build();
        StagingSchleppbetriebEintrag b = StagingSchleppbetriebEintrag.builder().externalId(42).build();
        when(repository.findExistingExternalIds(java.util.List.of(42))).thenReturn(java.util.List.of());

        service.importIdempotent(List.of(a, b));

        // saveAll must be called with exactly one entry (external_id 42).
        org.mockito.ArgumentCaptor<List<StagingSchleppbetriebEintrag>> captor =
                org.mockito.ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.atLeastOnce()).saveAll(captor.capture());
        long totalSaved = captor.getAllValues().stream().mapToLong(List::size).sum();
        assertThat(totalSaved).isEqualTo(1);
    }

    @Test
    void importFromStream_throwsOnNullStream() {
        assertThatThrownBy(() -> service.importFromStream(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void importIdempotent_savesNewEntries() {
        StagingSchleppbetriebEintrag entry = StagingSchleppbetriebEintrag.builder()
                .externalId(198765)
                .build();
        when(repository.findExistingExternalIds(List.of(198765))).thenReturn(List.of());

        service.importIdempotent(List.of(entry));

        org.mockito.Mockito.verify(repository).saveAll(List.of(entry));
    }

    @Test
    void importIdempotent_skipsKnownExternalId() {
        StagingSchleppbetriebEintrag known = StagingSchleppbetriebEintrag.builder()
                .externalId(198765)
                .build();
        when(repository.findExistingExternalIds(List.of(198765))).thenReturn(List.of(198765));

        service.importIdempotent(List.of(known));

        // The known external_id gets filtered out -> nothing new gets stored.
        // saveAll must only ever have been called with empty lists.
        org.mockito.ArgumentCaptor<List<StagingSchleppbetriebEintrag>> captor =
                org.mockito.ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.atLeastOnce()).saveAll(captor.capture());
        long totalSaved = captor.getAllValues().stream().mapToLong(List::size).sum();
        assertThat(totalSaved).isEqualTo(0);
    }
}
