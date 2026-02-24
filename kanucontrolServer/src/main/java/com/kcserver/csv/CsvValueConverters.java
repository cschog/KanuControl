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

    public static Object convert(String fieldName, String converter, String raw) {

        return switch (converter) {

            case "trim" -> raw.trim();

            case "date_de" -> parseDateDe(raw);

            case "sex_de" -> parseSexDe(raw);

            case "bool_ja_nein" -> parseBooleanJaNein(raw);

            case "iban" -> {
                String normalized = raw.replaceAll("\\s+", "").toUpperCase();
                if (!isValidIban(normalized)) {
                    throw new CsvFieldException(
                            fieldName,
                            raw,
                            "Ungültige IBAN"
                    );
                }
                yield normalized;
            }

            case "bic" -> {
                String bic = normalizeBic(raw);
                yield bic;
            }

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

    private static boolean isValidIban(String iban) {

        if (iban.length() < 15 || iban.length() > 34) {
            return false;
        }

        // Erste 4 Zeichen nach hinten
        String rearranged = iban.substring(4) + iban.substring(0, 4);

        // Buchstaben → Zahlen (A=10 ... Z=35)
        StringBuilder numeric = new StringBuilder();

        for (char c : rearranged.toCharArray()) {
            if (Character.isDigit(c)) {
                numeric.append(c);
            } else if (Character.isLetter(c)) {
                numeric.append((int) c - 55);
            } else {
                return false;
            }
        }

        // Mod 97
        int mod = 0;
        for (int i = 0; i < numeric.length(); i++) {
            mod = (mod * 10 + (numeric.charAt(i) - '0')) % 97;
        }

        return mod == 1;
    }

    private static String normalizeBic(String raw) {

        String bic = raw.replaceAll("\\s+", "").toUpperCase();

        if (!bic.matches("^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$")) {
            throw new IllegalArgumentException("Ungültige BIC: " + raw);
        }

        return bic;
    }
}