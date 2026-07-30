package de.windenshelter.flugbuch.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import de.windenshelter.flugbuch.model.StagingMainFlightLog;
import de.windenshelter.flugbuch.service.ftp.FtpFileFetcher;

/**
 * Proves that the FTP-download step doesn't need a real FTP server to be
 * testable: {@link FtpFileFetcher} is an interface, so here it's replaced
 * with a mock that just returns a CSV file already sitting on disk, and we
 * only verify how {@link FlightLogFtpImportScheduler} reacts to that.
 * {@link MainFlightLogImportService} is also mocked, since its own parsing
 * and idempotency behaviour is already covered by
 * {@code MainFlightLogImportServiceTest} and
 * {@code MainFlightLogImportIntegrationTest}.
 */
class FlightLogFtpImportSchedulerTest {

    private static final String HEADER =
            "Datum;Startzeit;Landezeit;Muster;Kennzeichen;Pilot;Gäste;Flugart;Startplatz;Zielplatz;Flugleiter;Geschleppter;Schlepphöhe;Betrag;Bemerkung;Fluganzahl";

    private final FtpFileFetcher ftpFileFetcher = mock(FtpFileFetcher.class);
    private final MainFlightLogImportService mainFlightLogImportService = mock(MainFlightLogImportService.class);
    private final FlightLogFtpImportScheduler scheduler =
            new FlightLogFtpImportScheduler(ftpFileFetcher, mainFlightLogImportService);

    private Path tempCsvFile;

    @AfterEach
    void cleanUp() throws IOException {
        if (tempCsvFile != null) {
            Files.deleteIfExists(tempCsvFile);
        }
    }

    // Happy path: a real (temp) CSV file is "downloaded", then handed to the importer as usual.
    @Test
    void importDailyFlightLog_downloadsThenImportsIdempotently() throws IOException {
        String csv = HEADER + "\n"
                + "16.12.2025;09:30;09:58;\"ASK 21\";D-1234;\"Max Mustermann\";0;Schulung;\"EDKA\";\"EDKA\";\"Erika Musterfrau\";;;15.0;;1\n";
        tempCsvFile = Files.createTempFile("flugbuch-test", ".csv");
        Files.writeString(tempCsvFile, csv, StandardCharsets.UTF_8);

        when(ftpFileFetcher.fetchLatestFile()).thenReturn(tempCsvFile);
        List<StagingMainFlightLog> parsedEntries = List.of(new StagingMainFlightLog());
        when(mainFlightLogImportService.importFromStream(any())).thenReturn(parsedEntries);

        scheduler.importDailyFlightLog();

        verify(mainFlightLogImportService).importIdempotent(parsedEntries);
    }

    // If the FTP download itself fails, the importer must never be called at all.
    @Test
    void importDailyFlightLog_downloadFails_doesNotAttemptImport() {
        when(ftpFileFetcher.fetchLatestFile()).thenThrow(new FtpDownloadException("FTP server unreachable"));

        scheduler.importDailyFlightLog();

        verifyNoInteractions(mainFlightLogImportService);
    }

    @Test
    void importDailyFlightLog_downloadedFileMissing_doesNotCallImportIdempotent() {
        // fetchLatestFile "succeeds" but points at a file that isn't actually there,
        // simulating e.g. a race condition or a disk issue after the download.
        Path missingFile = Path.of("/tmp/this-file-does-not-exist-" + System.nanoTime() + ".csv");
        when(ftpFileFetcher.fetchLatestFile()).thenReturn(missingFile);

        scheduler.importDailyFlightLog();

        verify(mainFlightLogImportService, never()).importIdempotent(anyList());
    }
}
