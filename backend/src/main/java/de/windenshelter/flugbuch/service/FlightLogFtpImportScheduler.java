package de.windenshelter.flugbuch.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.windenshelter.flugbuch.model.StagingMainFlightLog;
import de.windenshelter.flugbuch.service.ftp.FtpFileFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Runs daily at 22:00 - an hour after the upstream server is expected to
 * receive that day's export at 21:00, to leave some margin - and imports
 * that day's flight log CSV into the staging table (ticket #29).
 *
 * Downloading and importing are deliberately two separate steps: if the
 * import fails (e.g. the database is briefly unavailable), the CSV is
 * already saved on the local volume and the import can be retried without
 * fetching it again from the FTP server.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlightLogFtpImportScheduler {

    private final FtpFileFetcher ftpFileFetcher;
    private final MainFlightLogImportService mainFlightLogImportService;

    /** Downloads the daily CSV and imports it; logs and gives up cleanly if either step fails. */
    @Scheduled(cron = "0 0 22 * * *")
    public void importDailyFlightLog() {
        Path localFile;
        try {
            localFile = ftpFileFetcher.fetchLatestFile();
        } catch (FtpDownloadException e) {
            log.error("Daily flight log FTP download failed: {}", e.getMessage(), e);
            return;
        }

        try (InputStream csvContent = Files.newInputStream(localFile)) {
            List<StagingMainFlightLog> entries = mainFlightLogImportService.importFromStream(csvContent);
            mainFlightLogImportService.importIdempotent(entries);
        } catch (IOException e) {
            log.error("Could not read downloaded flight log CSV at {}: {}", localFile, e.getMessage(), e);
        }
    }
}
