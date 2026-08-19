package de.windenshelter.flugbuch.service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Runs daily at 23:00 - after the FTP import at 22:00 (see
 * {@link FlightLogFtpImportScheduler}), so that day's flights are already in
 * the staging table - and prints that day's flight log to a PDF file on the
 * print output volume.
 *
 * A failure here (e.g. the output directory is unwritable) is logged and
 * swallowed rather than rethrown: there's no caller waiting on this scheduled
 * job, and letting the exception escape would just produce a noisy stack
 * trace in the scheduler's thread without helping anyone.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyFlightLogPrintScheduler {

    private final DailyFlightLogPrintService dailyFlightLogPrintService;

    @Scheduled(cron = "0 0 23 * * *")
    public void printDailyFlightLog() {
        LocalDate today = LocalDate.now();
        try {
            Path outputFile = dailyFlightLogPrintService.generateDailyPrint(today);
            log.info("Daily flight log printout for {} written to {}", today, outputFile);
        } catch (IOException e) {
            log.error("Daily flight log printout for {} failed: {}", today, e.getMessage(), e);
        }
    }
}
