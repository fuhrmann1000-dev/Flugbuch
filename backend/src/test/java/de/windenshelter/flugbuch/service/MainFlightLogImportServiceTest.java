package de.windenshelter.flugbuch.service;

import de.windenshelter.flugbuch.model.StagingMainFlightLog;
import de.windenshelter.flugbuch.repository.MainFlightLogStagingRepository;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MainFlightLogImportServiceTest {

    private final MainFlightLogStagingRepository repository = mock(MainFlightLogStagingRepository.class);
    private final MainFlightLogImportService service = new MainFlightLogImportService(repository);

    private static final String HEADER =
            "Datum;Startzeit;Landezeit;Muster;Kennzeichen;Pilot;Gäste;Flugart;Startplatz;Zielplatz;Flugleiter;Geschleppter;Schlepphöhe;Betrag;Bemerkung;Fluganzahl";

    @Test
    void importFromStream_extrahiertEineGueltigeZeile() {
        String csv = HEADER + "\n"
                + "16.12.2025;09:30;09:58;\"Minimum \";D-MIBY;\"Martin Odening\";0;VFR;\"Altes Lager\";\"Altes Lager \";\"Zur Hilfe befähigte Person\";;;0.0;;1\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingMainFlightLog> result = service.importFromStream(stream);

        assertThat(result).hasSize(1);
        StagingMainFlightLog eintrag = result.get(0);
        assertThat(eintrag.getDatum()).isEqualTo(LocalDate.of(2025, 12, 16));
        assertThat(eintrag.getStartzeit()).isEqualTo(LocalTime.of(9, 30));
        assertThat(eintrag.getLandezeit()).isEqualTo(LocalTime.of(9, 58));
        assertThat(eintrag.getMuster()).isEqualTo("Minimum");
        assertThat(eintrag.getKennzeichen()).isEqualTo("D-MIBY");
        assertThat(eintrag.getPilot()).isEqualTo("Martin Odening");
        assertThat(eintrag.getFlugLeiter()).isEqualTo("Zur Hilfe befähigte Person");
        assertThat(eintrag.getFlugAnzahl()).isEqualTo(1);
    }

    @Test
    void importFromStream_ueberspringtHeader() {
        String csv = HEADER + "\n"
                + "16.12.2025;09:30;09:58;Minimum;D-MIBY;Pilot;0;VFR;A;B;Leiter;;;0.0;;1\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingMainFlightLog> result = service.importFromStream(stream);

        assertThat(result).hasSize(1);
    }

    @Test
    void importFromStream_parseGeschlepptenUndSchlepphoehe() {
        String csv = HEADER + "\n"
                + "12.12.2025;13:40;13:50;\"Merlin 1200\";D-MVBO;\"Odening, Martin\";0;\"Schlepp DoSi\";\"SLP Altes Lager\";\"SLP Altes Lager\";\"Kienöl, Volkmar\";\"Jeniya (Raimbekova, Yevgeniya)\";500;0.0;;1\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingMainFlightLog> result = service.importFromStream(stream);

        assertThat(result.get(0).getGeschleppter()).isEqualTo("Jeniya (Raimbekova, Yevgeniya)");
        assertThat(result.get(0).getSchleppHoehe()).isEqualTo(500);
    }

    @Test
    void importFromStream_toleriertUtf8Bom() {
        String csv = "﻿" + HEADER + "\n"
                + "16.12.2025;09:30;09:58;Minimum;D-MIBY;Pilot;0;VFR;A;B;Leiter;;;0.0;;1\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingMainFlightLog> result = service.importFromStream(stream);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKennzeichen()).isEqualTo("D-MIBY");
    }

    @Test
    void importFromStream_erhaeltUmlauteInNamen() {
        String csv = HEADER + "\n"
                + "22.11.2025;13:15;13:30;Solanus;D-MESI;V.Kienöl;0;check;\"Altes Lager\";\"Altes Lager\";\"Martin Odening\";;;0.0;;1\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingMainFlightLog> result = service.importFromStream(stream);

        assertThat(result.get(0).getPilot()).isEqualTo("V.Kienöl");
    }

    @Test
    void importFromStream_wirftBeiNullStream() {
        assertThatThrownBy(() -> service.importFromStream(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void importIdempotent_entferntDuplikateInnerhalbDerEingabe() {
        // Gleiches Datum+Startzeit+Kennzeichen zweimal in derselben Eingabe -> nur einmal speichern
        StagingMainFlightLog a = flug(LocalDate.of(2025, 12, 16), LocalTime.of(9, 30), "D-MIBY");
        StagingMainFlightLog b = flug(LocalDate.of(2025, 12, 16), LocalTime.of(9, 30), "D-MIBY");
        when(repository.findByLicensePlateInAndDateIn(anyCollection(), anyCollection())).thenReturn(List.of());

        service.importIdempotent(List.of(a, b));

        verifyGespeichertCount(1);
    }

    @Test
    void importIdempotent_speichertNeuenFlug() {
        StagingMainFlightLog neu = flug(LocalDate.of(2025, 12, 16), LocalTime.of(9, 30), "D-MIBY");
        when(repository.findByLicensePlateInAndDateIn(anyCollection(), anyCollection())).thenReturn(List.of());

        service.importIdempotent(List.of(neu));

        org.mockito.Mockito.verify(repository).saveAll(List.of(neu));
    }

    @Test
    void importIdempotent_ueberspringtBekanntenFlug() {
        StagingMainFlightLog neuerVersuch = flug(LocalDate.of(2025, 12, 16), LocalTime.of(9, 30), "D-MIBY");
        StagingMainFlightLog bereitsGespeichert = flug(LocalDate.of(2025, 12, 16), LocalTime.of(9, 30), "D-MIBY");
        when(repository.findByLicensePlateInAndDateIn(anyCollection(), anyCollection()))
                .thenReturn(List.of(bereitsGespeichert));

        service.importIdempotent(List.of(neuerVersuch));

        verifyGespeichertCount(0);
    }

    private StagingMainFlightLog flug(LocalDate datum, LocalTime startzeit, String kennzeichen) {
        return StagingMainFlightLog.builder()
                .datum(datum)
                .startzeit(startzeit)
                .kennzeichen(kennzeichen)
                .build();
    }

    private void verifyGespeichertCount(int expected) {
        org.mockito.ArgumentCaptor<List<StagingMainFlightLog>> captor =
                org.mockito.ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.atLeastOnce()).saveAll(captor.capture());
        long gesamtGespeichert = captor.getAllValues().stream().mapToLong(List::size).sum();
        assertThat(gesamtGespeichert).isEqualTo(expected);
    }
}