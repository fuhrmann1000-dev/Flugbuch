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

    private static final String HEADER =
            "ID;Verein;Zeitpunkt;\"Piloten Nr\";Pilot;Typ;\"Windenfahrer Nr\";Windenfahrer;\"Startleiter Nr\";Startleiter;Winde;Zusatz";

    @Test
    void importiereAusStream_extrahiertEineGueltigeZeile() {
        String csv = HEADER + "\n"
                + "198765;12;01.05.2026 10:15;3421;\"Mustermann, Max\";GS Solo;512;\"Schmidt, Anna\";207;\"Weber, Tom\";Felix 1;\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingSchleppbetriebEintrag> result = service.importiereAusStream(stream);

        assertThat(result).hasSize(1);
        StagingSchleppbetriebEintrag eintrag = result.get(0);
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

        List<StagingSchleppbetriebEintrag> result = service.importiereAusStream(stream);

        assertThat(result).hasSize(1);
    }

    @Test
    void importiereAusStream_parseDeutschesDatumsformat() {
        String csv = HEADER + "\n"
                + "198765;12;15.07.2025 18:42;3421;Pilot;Solo;512;Winde;207;Leiter;Felix 1;\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingSchleppbetriebEintrag> result = service.importiereAusStream(stream);

        assertThat(result.get(0).getZeitpunkt()).isEqualTo(LocalDateTime.of(2025, 7, 15, 18, 42));
    }

    @Test
    void importiereAusStream_parseQuotedZeitpunkt() {
        // Echter schleppbetrieb.de-Export setzt das Zeitpunkt-Feld in Anfuehrungszeichen
        String csv = HEADER + "\n"
                + "17590;12;\"18.04.2026 14:46\";1057;\"Bernd Mueller\";\"GS Solo\";90;\"Ralf Stein\";498;\"Maja Goetz\";\"Felix 1\";\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingSchleppbetriebEintrag> result = service.importiereAusStream(stream);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getZeitpunkt()).isEqualTo(LocalDateTime.of(2026, 4, 18, 14, 46));
        assertThat(result.get(0).getWindeName()).isEqualTo("Felix 1");
    }

    @Test
    void importiereAusStream_toleriertUtf8Bom() {
        // Echter Export beginnt mit UTF-8-BOM (EF BB BF) vor der Kopfzeile
        String csv = "﻿" + HEADER + "\n"
                + "17590;12;\"18.04.2026 14:46\";1057;\"Bernd Mueller\";\"GS Solo\";90;\"Ralf Stein\";498;\"Maja Goetz\";\"Felix 1\";\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingSchleppbetriebEintrag> result = service.importiereAusStream(stream);

        // BOM sitzt auf der Kopfzeile, die uebersprungen wird -> Datenzeile bleibt sauber
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExternalId()).isEqualTo(17590);
    }

    @Test
    void importiereAusStream_erhaeltUmlauteInNamen() {
        String csv = HEADER + "\n"
                + "17587;12;\"18.04.2026 14:37\";634;\"Gesa Schütze\";\"GS Solo\";107;\"Uwe Müller\";498;\"Maja Götz\";\"GSW 1\";\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingSchleppbetriebEintrag> result = service.importiereAusStream(stream);

        assertThat(result.get(0).getPilot()).isEqualTo("Gesa Schütze");
        assertThat(result.get(0).getWindenfahrer()).isEqualTo("Uwe Müller");
    }

    @Test
    void importiereAusStream_erhaeltInternationaleNamen() {
        // UTF-8 muss end-to-end erhalten bleiben: arabisch, chinesisch, franzoesisch, kyrillisch
        String csv = HEADER + "\n"
                + "1;12;\"01.05.2026 10:00\";1;\"محمد علي\";Solo;2;\"李伟\";3;\"François Léveillé\";Felix 1;\n"
                + "2;12;\"01.05.2026 10:05\";4;\"Иван Петров\";Solo;2;\"Müller-Œuvre\";3;\"Łukasz Brzęczyszczykiewicz\";Felix 1;\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingSchleppbetriebEintrag> result = service.importiereAusStream(stream);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPilot()).isEqualTo("محمد علي");        // Arabisch
        assertThat(result.get(0).getWindenfahrer()).isEqualTo("李伟");      // Chinesisch
        assertThat(result.get(0).getStartleiter()).isEqualTo("François Léveillé"); // Franzoesisch
        assertThat(result.get(1).getPilot()).isEqualTo("Иван Петров");     // Kyrillisch
        assertThat(result.get(1).getStartleiter()).isEqualTo("Łukasz Brzęczyszczykiewicz");
    }

    @Test
    void importiereIdempotent_entferntDuplikateInnerhalbDerEingabe() {
        // Gleiche external_id zweimal in derselben Eingabe -> nur einmal speichern
        StagingSchleppbetriebEintrag a = StagingSchleppbetriebEintrag.builder().externalId(42).build();
        StagingSchleppbetriebEintrag b = StagingSchleppbetriebEintrag.builder().externalId(42).build();
        when(repository.findExistingExternalIds(java.util.List.of(42))).thenReturn(java.util.List.of());

        service.importiereIdempotent(List.of(a, b));

        // saveAll wird mit genau einem Eintrag (external_id 42) aufgerufen
        org.mockito.ArgumentCaptor<List<StagingSchleppbetriebEintrag>> captor =
                org.mockito.ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.atLeastOnce()).saveAll(captor.capture());
        long gesamtGespeichert = captor.getAllValues().stream().mapToLong(List::size).sum();
        assertThat(gesamtGespeichert).isEqualTo(1);
    }

    @Test
    void importiereAusStream_wirftBeiNullStream() {
        assertThatThrownBy(() -> service.importiereAusStream(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void importiereIdempotent_speichertNeueEintraege() {
        StagingSchleppbetriebEintrag neu = StagingSchleppbetriebEintrag.builder()
                .externalId(198765)
                .build();
        when(repository.findExistingExternalIds(List.of(198765))).thenReturn(List.of());

        service.importiereIdempotent(List.of(neu));

        org.mockito.Mockito.verify(repository).saveAll(List.of(neu));
    }

    @Test
    void importiereIdempotent_ueberspringtBekannteExternalId() {
        StagingSchleppbetriebEintrag bekannt = StagingSchleppbetriebEintrag.builder()
                .externalId(198765)
                .build();
        when(repository.findExistingExternalIds(List.of(198765))).thenReturn(List.of(198765));

        service.importiereIdempotent(List.of(bekannt));

        // Bekannte external_id wird herausgefiltert -> nichts Neues wird gespeichert.
        // saveAll darf nur mit leeren Listen aufgerufen worden sein.
        org.mockito.ArgumentCaptor<List<StagingSchleppbetriebEintrag>> captor =
                org.mockito.ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.atLeastOnce()).saveAll(captor.capture());
        long gesamtGespeichert = captor.getAllValues().stream().mapToLong(List::size).sum();
        assertThat(gesamtGespeichert).isEqualTo(0);
    }
}
