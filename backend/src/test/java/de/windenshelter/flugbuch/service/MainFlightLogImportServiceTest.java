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

    // Header text matches the real main flight log CSV export format - left
    // in German since that's the actual external file format being parsed.
    private static final String HEADER =
            "Datum;Startzeit;Landezeit;Muster;Kennzeichen;Pilot;Gäste;Flugart;Startplatz;Zielplatz;Flugleiter;Geschleppter;Schlepphöhe;Betrag;Bemerkung;Fluganzahl";

    @Test
    void importFromStream_extractsOneValidRow() {
        String csv = HEADER + "\n"
                + "16.12.2025;09:30;09:58;\"Minimum \";D-MIBY;\"Martin Odening\";0;VFR;\"Altes Lager\";\"Altes Lager \";\"Zur Hilfe befähigte Person\";;;0.0;;1\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingMainFlightLog> result = service.importFromStream(stream);

        assertThat(result).hasSize(1);
        StagingMainFlightLog entry = result.get(0);
        assertThat(entry.getDatum()).isEqualTo(LocalDate.of(2025, 12, 16));
        assertThat(entry.getStartzeit()).isEqualTo(LocalTime.of(9, 30));
        assertThat(entry.getLandezeit()).isEqualTo(LocalTime.of(9, 58));
        assertThat(entry.getMuster()).isEqualTo("Minimum");
        assertThat(entry.getKennzeichen()).isEqualTo("D-MIBY");
        assertThat(entry.getPilot()).isEqualTo("Martin Odening");
        assertThat(entry.getFlugLeiter()).isEqualTo("Zur Hilfe befähigte Person");
        assertThat(entry.getFlugAnzahl()).isEqualTo(1);
    }

    @Test
    void importFromStream_skipsHeader() {
        String csv = HEADER + "\n"
                + "16.12.2025;09:30;09:58;Minimum;D-MIBY;Pilot;0;VFR;A;B;Leiter;;;0.0;;1\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingMainFlightLog> result = service.importFromStream(stream);

        assertThat(result).hasSize(1);
    }

    @Test
    void importFromStream_parsesTowedAircraftAndTowHeight() {
        String csv = HEADER + "\n"
                + "12.12.2025;13:40;13:50;\"Merlin 1200\";D-MVBO;\"Odening, Martin\";0;\"Schlepp DoSi\";\"SLP Altes Lager\";\"SLP Altes Lager\";\"Kienöl, Volkmar\";\"Jeniya (Raimbekova, Yevgeniya)\";500;0.0;;1\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingMainFlightLog> result = service.importFromStream(stream);

        assertThat(result.get(0).getGeschleppter()).isEqualTo("Jeniya (Raimbekova, Yevgeniya)");
        assertThat(result.get(0).getSchleppHoehe()).isEqualTo(500);
    }

    @Test
    void importFromStream_toleratesUtf8Bom() {
        String csv = "﻿" + HEADER + "\n"
                + "16.12.2025;09:30;09:58;Minimum;D-MIBY;Pilot;0;VFR;A;B;Leiter;;;0.0;;1\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingMainFlightLog> result = service.importFromStream(stream);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKennzeichen()).isEqualTo("D-MIBY");
    }

    @Test
    void importFromStream_preservesUmlautsInNames() {
        String csv = HEADER + "\n"
                + "22.11.2025;13:15;13:30;Solanus;D-MESI;V.Kienöl;0;check;\"Altes Lager\";\"Altes Lager\";\"Martin Odening\";;;0.0;;1\n";
        InputStream stream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<StagingMainFlightLog> result = service.importFromStream(stream);

        assertThat(result.get(0).getPilot()).isEqualTo("V.Kienöl");
    }

    @Test
    void importFromStream_throwsOnNullStream() {
        assertThatThrownBy(() -> service.importFromStream(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void importIdempotent_removesDuplicatesWithinTheInput() {
        // Same date+startTime+registration twice in the same input -> saved only once
        StagingMainFlightLog a = flight(LocalDate.of(2025, 12, 16), LocalTime.of(9, 30), "D-MIBY");
        StagingMainFlightLog b = flight(LocalDate.of(2025, 12, 16), LocalTime.of(9, 30), "D-MIBY");
        when(repository.findByLicensePlateInAndDateIn(anyCollection(), anyCollection())).thenReturn(List.of());

        var result = service.importIdempotent(List.of(a, b));

        verifySavedCount(1);
        assertThat(result.stored()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(1);
    }

    @Test
    void importIdempotent_savesNewFlight() {
        StagingMainFlightLog newFlight = flight(LocalDate.of(2025, 12, 16), LocalTime.of(9, 30), "D-MIBY");
        when(repository.findByLicensePlateInAndDateIn(anyCollection(), anyCollection())).thenReturn(List.of());

        var result = service.importIdempotent(List.of(newFlight));

        org.mockito.Mockito.verify(repository).saveAll(List.of(newFlight));
        assertThat(result.stored()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(0);
    }

    @Test
    void importIdempotent_skipsKnownFlight() {
        StagingMainFlightLog newAttempt = flight(LocalDate.of(2025, 12, 16), LocalTime.of(9, 30), "D-MIBY");
        StagingMainFlightLog alreadyStored = flight(LocalDate.of(2025, 12, 16), LocalTime.of(9, 30), "D-MIBY");
        when(repository.findByLicensePlateInAndDateIn(anyCollection(), anyCollection()))
                .thenReturn(List.of(alreadyStored));

        var result = service.importIdempotent(List.of(newAttempt));

        verifySavedCount(0);
        assertThat(result.stored()).isEqualTo(0);
        assertThat(result.skipped()).isEqualTo(1);
    }

    // Registration (Kennzeichen) is null for unregistered free-flight aircraft
    // (e.g. a winch launch with no registration requirement) - duplicate
    // detection must still catch these. See
    // MainFlightLogStagingRepository#findByLicensePlateInAndDateIn: a bugfix
    // there is what actually closes this duplicates ticket.
    @Test
    void importIdempotent_skipsKnownFlightWithoutRegistration() {
        StagingMainFlightLog newAttempt = flight(LocalDate.of(2025, 12, 16), LocalTime.of(9, 30), null);
        StagingMainFlightLog alreadyStored = flight(LocalDate.of(2025, 12, 16), LocalTime.of(9, 30), null);
        when(repository.findByLicensePlateInAndDateIn(anyCollection(), anyCollection()))
                .thenReturn(List.of(alreadyStored));

        service.importIdempotent(List.of(newAttempt));

        verifySavedCount(0);
    }

    private StagingMainFlightLog flight(LocalDate date, LocalTime startTime, String registration) {
        return StagingMainFlightLog.builder()
                .datum(date)
                .startzeit(startTime)
                .kennzeichen(registration)
                .build();
    }

    private void verifySavedCount(int expected) {
        org.mockito.ArgumentCaptor<List<StagingMainFlightLog>> captor =
                org.mockito.ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.atLeastOnce()).saveAll(captor.capture());
        long totalSaved = captor.getAllValues().stream().mapToLong(List::size).sum();
        assertThat(totalSaved).isEqualTo(expected);
    }
}
