package com.kcserver.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public final class StringUtils {

    private static final DateTimeFormatter GERMAN_DATE =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private StringUtils() {}

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public static String join(String separator, String... values) {
        return Arrays.stream(values)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .reduce((a, b) -> a + separator + b)
                .orElse(null);
    }

    public static String heute() {
        return LocalDate.now().format(GERMAN_DATE);
    }

    public static String formatDate(LocalDate date) {
        return date == null ? null : date.format(GERMAN_DATE);
    }

    public static String ortDatum(String ort) {
        return join(", ", ort, heute());
    }

    public static String name(String vorname, String name) {
        return join(" ", vorname, name);
    }

    public static String plzOrt(String plz, String ort) {
        return join(" ", plz, ort);
    }
}