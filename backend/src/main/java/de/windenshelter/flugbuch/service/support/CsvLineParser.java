package de.windenshelter.flugbuch.service.support;

import de.windenshelter.flugbuch.service.SchleppbetriebImportException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Common parsing logic for schleppbetrieb.de CSV exports
 * (towing logbook and main flight log). Both formats
 * are {@code ;}-separated with optional double quotes
 * around fields — only the column mapping differs per calling service.
 */
public final class CsvLineParser {

    private CsvLineParser() {
    }

    public static List<String> splitLine(String line, char splitChar) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == splitChar) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields;
    }

    public static String field(List<String> fields, int index) {
        if (index >= fields.size()) {
            return null;
        }
        String value = fields.get(index).trim();
        return value.isEmpty() ? null : value;
    }

    public static Integer parseInteger(List<String> fields, int index, int lineNumber) {
        String value = field(fields, index);
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new SchleppbetriebImportException(String.format(
                    "Line %d, Column %d: '%s' is not a valid integer.",
                    lineNumber, index, value), e);
        }
    }

    public static Double parseDouble(List<String> fields, int index, int lineNumeber) {
        String value = field(fields, index);
        if (value == null) {
            return null;
        }
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            throw new SchleppbetriebImportException(String.format(
                    "Line %d, Column %d: '%s' is not a valid number.",
                    lineNumeber, index, value), e);
        }
    }

    public static LocalDate parseDate(String value, DateTimeFormatter format, int lineNumber) {
        return parseTemporal(value, format, lineNumber, "Date (expected dd.MM.yyyy)", LocalDate::parse);
    }

    public static LocalTime parseTime(String value, DateTimeFormatter format, int lineNumber) {
        return parseTemporal(value, format, lineNumber, "Time (expected HH:mm)", LocalTime::parse);
    }

    public static LocalDateTime parseDateTime(String value, DateTimeFormatter format, int lineNumber) {
        return parseTemporal(value, format, lineNumber, "Date (expected dd.MM.yyyy HH:mm)", LocalDateTime::parse);
    }

    private static <T> T parseTemporal(String value, DateTimeFormatter format, int lineNumber,
                                       String expectedText,
                                       BiFunction<String, DateTimeFormatter, T> parser) {
        if (value == null) {
            return null;
        }
        try {
            return parser.apply(value, format);
        } catch (DateTimeParseException e) {
            throw new SchleppbetriebImportException(String.format(
                    "Line %d: '%s' is not a valid %s.", lineNumber, value, expectedText), e);
        }
    }
}