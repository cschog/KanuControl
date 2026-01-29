package com.kcserver.csv;

import com.kcserver.enumtype.Sex;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class CsvValueConverters {

    private static final DateTimeFormatter DATE_DE =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private CsvValueConverters() {
    }

    public static Object convert(String converter, String raw) {

        return switch (converter) {

            case "trim" -> raw.trim();

            case "date_de" -> parseDateDe(raw);

            case "sex_de" -> parseSexDe(raw);

            case "bool_ja_nein" -> parseBooleanJaNein(raw);

            default -> throw new IllegalArgumentException(
                    "Unbekannter CSV-Converter: " + converter
            );
        };
    }

    /* =========================
       KONVERTER
       ========================= */

    private static LocalDate parseDateDe(String raw) {
        return LocalDate.parse(raw.trim(), DATE_DE);
    }

    private static Sex parseSexDe(String raw) {

        String v = raw.trim().toLowerCase(Locale.ROOT);

        return switch (v) {
            case "m", "männlich", "maennlich" -> Sex.MAENNLICH;
            case "w", "weiblich" -> Sex.WEIBLICH;
            case "d", "divers" -> Sex.DIVERS;
            default -> throw new IllegalArgumentException(
                    "Unbekanntes Geschlecht: " + raw
            );
        };
    }

    private static Boolean parseBooleanJaNein(String raw) {

        String v = raw.trim().toLowerCase(Locale.ROOT);

        return switch (v) {
            case "ja", "j", "true", "1" -> true;
            case "nein", "n", "false", "0" -> false;
            default -> throw new IllegalArgumentException(
                    "Unbekannter Boolean-Wert: " + raw
            );
        };
    }
}