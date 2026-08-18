package de.windenshelter.flugbuch.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/**
 * Plain Mockito unit test for {@link DailyFlightLogPrintScheduler} - mirrors
 * {@link FlightLogFtpImportSchedulerTest}'s style. {@link DailyFlightLogPrintService}
 * is mocked since its own PDF-generation behaviour is covered by
 * {@code DailyFlightLogPrintServiceTest}; what matters here is only that the
 * scheduler calls it for today's date and never lets an IOException escape.
 */
class DailyFlightLogPrintSchedulerTest {

    private final DailyFlightLogPrintService dailyFlightLogPrintService = mock(DailyFlightLogPrintService.class);
    private final DailyFlightLogPrintScheduler scheduler =
            new DailyFlightLogPrintScheduler(dailyFlightLogPrintService);

    @Test
    void printDailyFlightLog_delegatesToServiceForToday() throws IOException {
        when(dailyFlightLogPrintService.generateDailyPrint(any(LocalDate.class)))
                .thenReturn(Path.of("/tmp/flugbuch-test.pdf"));

        scheduler.printDailyFlightLog();

        verify(dailyFlightLogPrintService).generateDailyPrint(LocalDate.now());
    }

    // If PDF generation fails (e.g. the output directory is unwritable), the
    // scheduler must log and give up cleanly rather than propagate the
    // exception - there's no caller to catch it in a scheduled job.
    @Test
    void printDailyFlightLog_serviceThrowsIOException_doesNotPropagate() throws IOException {
        when(dailyFlightLogPrintService.generateDailyPrint(any(LocalDate.class)))
                .thenThrow(new IOException("disk full"));

        scheduler.printDailyFlightLog();

        verify(dailyFlightLogPrintService).generateDailyPrint(LocalDate.now());
    }
}
