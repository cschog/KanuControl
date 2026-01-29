package com.kcserver.csv;

import java.time.LocalDate;
import java.util.Map;

public class CsvPersonRow {

    private final Map<String, String> row;

    public CsvPersonRow(Map<String, String> row) {
        this.row = row;
    }

    /* =========================
       BASIS
       ========================= */

    public String getRaw(String csvColumn) {
        return row.get(csvColumn);
    }

    /* =========================
       ZENTRALE API
       ========================= */

    public Object get(CsvFieldMapping mapping) {

        String raw = getRaw(mapping.csvColumn());

        if (raw == null || raw.isBlank()) {
            return null;
        }

        if (mapping.converter() == null || mapping.converter().isBlank()) {
            return raw;
        }

        return CsvValueConverters.convert(
                mapping.converter(),
                raw
        );
    }

    /* =========================
       TYP-HILFEN (optional)
       ========================= */

    public LocalDate getLocalDate(CsvFieldMapping mapping) {
        return (LocalDate) get(mapping);
    }
}