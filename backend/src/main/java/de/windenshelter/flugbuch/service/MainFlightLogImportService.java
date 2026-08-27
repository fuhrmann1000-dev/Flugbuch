package de.windenshelter.flugbuch.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.windenshelter.flugbuch.model.StagingMainFlightLog;
import de.windenshelter.flugbuch.repository.MainFlightLogStagingRepository;
import de.windenshelter.flugbuch.service.support.ChunkedDeduplicatingSaver;
import static de.windenshelter.flugbuch.service.support.CsvLineParser.field;
import static de.windenshelter.flugbuch.service.support.CsvLineParser.parseDate;
import static de.windenshelter.flugbuch.service.support.CsvLineParser.parseDouble;
import static de.windenshelter.flugbuch.service.support.CsvLineParser.parseInteger;
import static de.windenshelter.flugbuch.service.support.CsvLineParser.parseTime;
import static de.windenshelter.flugbuch.service.support.CsvLineParser.splitLine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainFlightLogImportService {

    private static final char DIVIDING_CHARACTER = ';';
    private static final int CHUNK_SIZE = 1000;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final int COLUMN_DATUM = 0;
    private static final int COLUMN_START_ZEIT = 1;
    private static final int COLUMN_LANDE_ZEIT = 2;
    private static final int COLUMN_MUSTER = 3;
    private static final int COLUMN_KENNZEICHEN = 4;
    private static final int COLUMN_PILOT = 5;
    private static final int COLUMN_GAESTE = 6;
    private static final int COLUMN_FLUGART = 7;
    private static final int COLUMN_START_PLATZ = 8;
    private static final int COLUMN_ZIEL_PLATZ = 9;
    private static final int COLUMN_FLUG_LEITER = 10;
    private static final int COLUMN_GESCHLEPPTER = 11;
    private static final int COLUMN_SCHLEPPHOEHE = 12;
    private static final int COLUMN_BETRAG = 13;
    private static final int COLUMN_BEMERKUNG = 14;
    private static final int COLUMN_FLUGANZAHL = 15;
    private static final int COLUMN_MINIMUM = 16;

    private final MainFlightLogStagingRepository stagingRepository;

    public List<StagingMainFlightLog> importFromStream(InputStream csvContent) {
        Objects.requireNonNull(csvContent, "InputStream must not be null");

        List<StagingMainFlightLog> resultList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(csvContent, StandardCharsets.UTF_8))) {

            String line;
            boolean headlineJumpedOver = false;
            int rowNumber = 0;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.isBlank()) {
                    continue;
                }
                if (!headlineJumpedOver) {
                    headlineJumpedOver = true;
                    continue;
                }
                resultList.add(processedRow(line, rowNumber));
            }

        } catch (IOException e) {
            log.error("Error reading main flight log CSV: {}", e.getMessage());
            throw new SchleppbetriebImportException("Main flight log CSV could not be read", e);
        }

        log.info("Main flight log import: {} records extracted.", resultList.size());
        return resultList;
    }

    @Transactional
    public void importIdempotent(List<StagingMainFlightLog> entries) {
        ChunkedDeduplicatingSaver<StagingMainFlightLog, NaturalKey> saver =
                new ChunkedDeduplicatingSaver<>(CHUNK_SIZE, NaturalKey::of, this::findKnownKeys);

        var result = saver.saveIdempotent(entries, stagingRepository::saveAll);

        log.info("Main flight log import idempotent: {} saved, {} already known/duplicated.",
                result.stored(), result.skipped());
    }

    private Set<NaturalKey> findKnownKeys(List<StagingMainFlightLog> chunk) {
        Set<String> licensePlateSet = chunk.stream()
                .map(StagingMainFlightLog::getKennzeichen)
                .collect(Collectors.toSet());
        Set<LocalDate> dateSet = chunk.stream()
                .map(StagingMainFlightLog::getDatum)
                .collect(Collectors.toSet());

        return stagingRepository.findByLicensePlateInAndDateIn(licensePlateSet, dateSet).stream()
                .map(NaturalKey::of)
                .collect(Collectors.toSet());
    }

    /** Natural key for duplicate detection, since the CSV does not provide an externalId. */
    private record NaturalKey(LocalDate date, LocalTime startTime, String licensePlate) {
        static NaturalKey of(StagingMainFlightLog entry) {
            return new NaturalKey(entry.getDatum(), entry.getStartzeit(), entry.getKennzeichen());
        }
    }

    private StagingMainFlightLog processedRow(String line, int lineNumber) {
        List<String> fields = splitLine(line, DIVIDING_CHARACTER);
        if (fields.size() < COLUMN_MINIMUM) {
            throw new SchleppbetriebImportException(String.format(
                    "Line %d has %d columns, expected at least %d.",
                    lineNumber, fields.size(), COLUMN_MINIMUM));
        }

        return StagingMainFlightLog.builder()
                .datum(parseDate(field(fields, COLUMN_DATUM), DATE_FORMAT, lineNumber))
                .startzeit(parseTime(field(fields, COLUMN_START_ZEIT), TIME_FORMAT, lineNumber))
                .landezeit(parseTime(field(fields, COLUMN_LANDE_ZEIT), TIME_FORMAT, lineNumber))
                .muster(field(fields, COLUMN_MUSTER))
                .kennzeichen(field(fields, COLUMN_KENNZEICHEN))
                .pilot(field(fields, COLUMN_PILOT))
                .gaeste(parseInteger(fields, COLUMN_GAESTE, lineNumber))
                .flugart(field(fields, COLUMN_FLUGART))
                .startPlatz(field(fields, COLUMN_START_PLATZ))
                .zielPlatz(field(fields, COLUMN_ZIEL_PLATZ))
                .flugLeiter(field(fields, COLUMN_FLUG_LEITER))
                .geschleppter(field(fields, COLUMN_GESCHLEPPTER))
                .schleppHoehe(parseInteger(fields, COLUMN_SCHLEPPHOEHE, lineNumber))
                .betrag(parseDouble(fields, COLUMN_BETRAG, lineNumber))
                .bemerkung(field(fields, COLUMN_BEMERKUNG))
                .flugAnzahl(parseInteger(fields, COLUMN_FLUGANZAHL, lineNumber))
                .build();
    }
}