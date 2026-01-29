package com.kcserver.csv.converter;

import com.kcserver.enumtype.Sex;

public class SexDeConverter implements CsvValueConverter {

    @Override
    public Sex convert(String raw) {

        if (raw == null || raw.isBlank()) {
            return null;
        }

        return switch (raw.trim().toLowerCase()) {
            case "m", "männlich", "maennlich" -> Sex.MAENNLICH;
            case "w", "weiblich"             -> Sex.WEIBLICH;
            case "d", "divers"               -> Sex.DIVERS;
            default -> throw new IllegalArgumentException(
                    "Unbekanntes Geschlecht: " + raw
            );
        };
    }
}