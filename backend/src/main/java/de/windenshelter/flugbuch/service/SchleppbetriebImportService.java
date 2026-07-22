package de.windenshelter.flugbuch.service;

import de.windenshelter.flugbuch.model.StagingSchleppbetriebEintrag;
import de.windenshelter.flugbuch.repository.SchleppbetriebStagingRepository;
import de.windenshelter.flugbuch.service.support.ChunkedDeduplicatingSaver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static de.windenshelter.flugbuch.service.support.CsvLineParser.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchleppbetriebImportService {

    private static final char DIVIDING_CHARACTER = ';';
    private static final String STATUS_PENDING = "PENDING";
    private static final int CHUNK_SIZE = 1000;
    private static final DateTimeFormatter DAYTIME_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final int COLUMN_ID = 0;
    private static final int COLUMN_VEREIN = 1;
    private static final int COLUMN_ZEITPUNKT = 2;
    private static final int COLUMN_PILOT_NR = 3;
    private static final int COLUMN_PILOT = 4;
    private static final int COLUMN_TYP = 5;
    private static final int COLUMN_WINDENFAHRER_NR = 6;
    private static final int COLUMN_WINDENFAHRER = 7;
    private static final int COLUMN_STARTLEITER_NR = 8;
    private static final int COLUMN_STARTLEITER = 9;
    private static final int COLUMN_WINDE = 10;
    private static final int COLUMN_ZUSATZ = 11;
    private static final int COLUMN_MINIMUM = COLUMN_WINDE + 1;

    private final SchleppbetriebStagingRepository stagingRepository;

    public List<StagingSchleppbetriebEintrag> importFromStream(InputStream csvContent) {
        Objects.requireNonNull(csvContent, "InputStream must not be null");

        List<StagingSchleppbetriebEintrag> resultList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(csvContent, StandardCharsets.UTF_8))) {

            String line;
            boolean headerSkipped = false;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                resultList.add(processLine(line, lineNumber));
            }

        } catch (IOException e) {
            log.error("Error reading towing log CSV: {}", e.getMessage());
            throw new SchleppbetriebImportException("Failed to read towing log CSV", e);
        }

        log.info("Towing log import: {} datasets extracted.", resultList.size());
        return resultList;
    }

    @Transactional
    public void importIdempotent(List<StagingSchleppbetriebEintrag> entries) {
        List<StagingSchleppbetriebEintrag> withExternalId = entries.stream()
                .filter(e -> e.getExternalId() != null)
                .toList();
        List<StagingSchleppbetriebEintrag> withoutExternalId = entries.stream()
                .filter(e -> e.getExternalId() == null)
                .toList();

        ChunkedDeduplicatingSaver<StagingSchleppbetriebEintrag, Integer> saver =
                new ChunkedDeduplicatingSaver<>(CHUNK_SIZE,
                        StagingSchleppbetriebEintrag::getExternalId,
                        chunk -> {
                            List<Integer> ids = chunk.stream()
                                    .map(StagingSchleppbetriebEintrag::getExternalId)
                                    .toList();
                            return new HashSet<>(stagingRepository.findExistingExternalIds(ids));
                        });

        var result = saver.saveIdempotent(withExternalId, stagingRepository::saveAll);

        // Entries without external_id are not dedupable -> always save.
        stagingRepository.saveAll(withoutExternalId);

        log.info("Tow ledger import idempotent: {} saved, {} already known/duplicated.",
                result.stored() + withoutExternalId.size(), result.skipped());
    }

    private StagingSchleppbetriebEintrag processLine(String line, int lineNumber) {
        List<String> fields = splitLine(line, DIVIDING_CHARACTER);
        if (fields.size() < COLUMN_MINIMUM) {
            throw new SchleppbetriebImportException(String.format(
                    "Line %d has %d columns, expected at least %d.",
                    lineNumber, fields.size(), COLUMN_MINIMUM));
        }

        return StagingSchleppbetriebEintrag.builder()
                .externalId(parseInteger(fields, COLUMN_ID, lineNumber))
                .vereinId(parseInteger(fields, COLUMN_VEREIN, lineNumber))
                .zeitpunkt(parseDateTime(field(fields, COLUMN_ZEITPUNKT), DAYTIME_FORMAT, lineNumber))
                .pilotNr(parseInteger(fields, COLUMN_PILOT_NR, lineNumber))
                .pilot(field(fields, COLUMN_PILOT))
                .typ(field(fields, COLUMN_TYP))
                .windenfahrerNr(parseInteger(fields, COLUMN_WINDENFAHRER_NR, lineNumber))
                .windenfahrer(field(fields, COLUMN_WINDENFAHRER))
                .startleiterNr(parseInteger(fields, COLUMN_STARTLEITER_NR, lineNumber))
                .startleiter(field(fields, COLUMN_STARTLEITER))
                .windeName(field(fields, COLUMN_WINDE))
                .zusatz(field(fields, COLUMN_ZUSATZ))
                .status(STATUS_PENDING)
                .build();
    }
}