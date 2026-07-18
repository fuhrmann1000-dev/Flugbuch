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
 * Gemeinsame Parsing-Logik fuer die schleppbetrieb.de-CSV-Exporte
 * (Schleppkladde und Hauptflugbuch). Beide Formate sind {@code ;}-separiert
 * mit optionalen doppelten Anfuehrungszeichen um Felder — nur die
 * Spaltenzuordnung unterscheidet sich pro aufrufendem Service.
 */
public final class CsvLineParser {

    private CsvLineParser() {
    }

    public static List<String> splitLine(String zeile, char trennzeichen) {
        List<String> felder = new ArrayList<>();
        StringBuilder aktuell = new StringBuilder();
        boolean inAnfuehrung = false;

        for (int i = 0; i < zeile.length(); i++) {
            char c = zeile.charAt(i);
            if (inAnfuehrung) {
                if (c == '"') {
                    if (i + 1 < zeile.length() && zeile.charAt(i + 1) == '"') {
                        aktuell.append('"');
                        i++;
                    } else {
                        inAnfuehrung = false;
                    }
                } else {
                    aktuell.append(c);
                }
            } else if (c == '"') {
                inAnfuehrung = true;
            } else if (c == trennzeichen) {
                felder.add(aktuell.toString());
                aktuell.setLength(0);
            } else {
                aktuell.append(c);
            }
        }
        felder.add(aktuell.toString());
        return felder;
    }

    public static String field(List<String> felder, int index) {
        if (index >= felder.size()) {
            return null;
        }
        String wert = felder.get(index).trim();
        return wert.isEmpty() ? null : wert;
    }

    public static Integer parseInteger(List<String> felder, int index, int zeilennummer) {
        String wert = field(felder, index);
        if (wert == null) {
            return null;
        }
        try {
            return Integer.valueOf(wert);
        } catch (NumberFormatException e) {
            throw new SchleppbetriebImportException(String.format(
                    "Zeile %d, Spalte %d: '%s' ist keine gueltige Ganzzahl.",
                    zeilennummer, index, wert), e);
        }
    }

    public static Double parseDouble(List<String> felder, int index, int zeilennummer) {
        String wert = field(felder, index);
        if (wert == null) {
            return null;
        }
        try {
            return Double.valueOf(wert);
        } catch (NumberFormatException e) {
            throw new SchleppbetriebImportException(String.format(
                    "Zeile %d, Spalte %d: '%s' ist keine gueltige Zahl.",
                    zeilennummer, index, wert), e);
        }
    }

    public static LocalDate parseDate(String wert, DateTimeFormatter format, int zeilennummer) {
        return parseTemporal(wert, format, zeilennummer, "Datum (erwartet dd.MM.yyyy)", LocalDate::parse);
    }

    public static LocalTime parseTime(String wert, DateTimeFormatter format, int zeilennummer) {
        return parseTemporal(wert, format, zeilennummer, "Uhrzeit (erwartet HH:mm)", LocalTime::parse);
    }

    public static LocalDateTime parseDateTime(String wert, DateTimeFormatter format, int zeilennummer) {
        return parseTemporal(wert, format, zeilennummer, "Datum (erwartet dd.MM.yyyy HH:mm)", LocalDateTime::parse);
    }

    private static <T> T parseTemporal(String wert, DateTimeFormatter format, int zeilennummer,
                                       String erwartungstext,
                                       BiFunction<String, DateTimeFormatter, T> parser) {
        if (wert == null) {
            return null;
        }
        try {
            return parser.apply(wert, format);
        } catch (DateTimeParseException e) {
            throw new SchleppbetriebImportException(String.format(
                    "Zeile %d: '%s' ist kein gueltiges %s.", zeilennummer, wert, erwartungstext), e);
        }
    }
}