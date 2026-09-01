package de.windenshelter.flugbuch.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import de.windenshelter.flugbuch.dto.ImportResultDto;
import de.windenshelter.flugbuch.model.StagingMainFlightLog;
import de.windenshelter.flugbuch.service.MainFlightLogImportService;
import de.windenshelter.flugbuch.service.SchleppbetriebImportException;
import de.windenshelter.flugbuch.service.support.ChunkedDeduplicatingSaver;
import lombok.RequiredArgsConstructor;

/**
 * Manual CSV upload for the main flight log, replacing the legacy
 * "daten_importieren.php" page. Any logged-in pilot can use it (see
 * {@code SecurityConfig}'s default {@code anyRequest().authenticated()} -
 * no extra rule needed here); it feeds the same staging table as the
 * existing nightly FTP import (ticket #29, see
 * {@code FlightLogFtpImportScheduler}), so re-uploading an already-imported
 * file is harmless - duplicates are silently skipped, not double-stored.
 */
@RestController
@RequestMapping("/api/v1/imports")
@RequiredArgsConstructor
public class ImportController {

    private final MainFlightLogImportService mainFlightLogImportService;

    /**
     * Parses and stores the uploaded CSV. There is no global exception
     * handler in this app, so malformed-file errors are caught here and
     * turned into a 400 with a message the frontend can show directly,
     * rather than surfacing as an opaque 500.
     */
    @PostMapping("/main-flight-log")
    public ImportResultDto importMainFlightLog(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file was uploaded");
        }

        try {
            List<StagingMainFlightLog> entries = mainFlightLogImportService.importFromStream(file.getInputStream());
            ChunkedDeduplicatingSaver.Result result = mainFlightLogImportService.importIdempotent(entries);
            return ImportResultDto.builder()
                    .imported(result.stored())
                    .skipped(result.skipped())
                    .build();
        } catch (SchleppbetriebImportException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The uploaded file could not be read");
        }
    }
}
