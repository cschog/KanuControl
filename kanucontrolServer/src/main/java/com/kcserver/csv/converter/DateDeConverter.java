package com.kcserver.csv.converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateDeConverter implements CsvValueConverter {

    private static final DateTimeFormatter DE =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public LocalDate convert(String raw) {

        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(raw.trim(), DE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Ungültiges Datum (erwartet TT.MM.JJJJ): " + raw
            );
        }
    }
}