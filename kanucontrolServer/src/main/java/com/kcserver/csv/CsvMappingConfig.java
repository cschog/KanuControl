package com.kcserver.csv;

import org.apache.commons.csv.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CsvMappingConfig {

    private final Map<String, CsvFieldMapping> byTargetField = new HashMap<>();

    /* =====================================================
       DEFAULT MAPPING
       ===================================================== */

    public static CsvMappingConfig defaultMapping() {

        CsvMappingConfig cfg = new CsvMappingConfig();

        cfg.put("vorname", "Vorname", null);
        cfg.put("name", "Nachname", null);
        cfg.put("sex", "Geschlecht", "sex_de");
        cfg.put("geburtsdatum", "Geburtsdatum", "date_de");

        cfg.put("telefon", "Telefon Mobil", null);
        cfg.put("telefonFestnetz", "Telefon Privat", null);
        cfg.put("email", "E-Mail", null);

        cfg.put("strasse", "Adresse", null);
        cfg.put("plz", "PLZ", null);
        cfg.put("ort", "Ort", null);

        return cfg;
    }

    private void put(String targetField, String csvColumn, String converter) {
        byTargetField.put(
                targetField.trim(),
                new CsvFieldMapping(csvColumn.trim(), targetField.trim(), converter)
        );
    }

    /* =====================================================
       EXISTIERENDER CODE (unverändert)
       ===================================================== */
    public static CsvMappingConfig load(InputStream mappingCsv) {

        CsvMappingConfig cfg = new CsvMappingConfig();

        try (Reader r = new InputStreamReader(mappingCsv, StandardCharsets.UTF_8)) {

            CSVParser parser = CSVFormat.Builder.create()
                    .setDelimiter(';')
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build()
                    .parse(r);

            for (CSVRecord rec : parser) {

                String converter =
                        rec.isMapped("converter")
                                ? rec.get("converter").trim()
                                : null;

                cfg.byTargetField.put(
                        rec.get("target_field").trim(),
                        new CsvFieldMapping(
                                rec.get("csv_column").trim(),
                                rec.get("target_field").trim(),
                                converter == null || converter.isBlank() ? null : converter
                        )
                );
            }

        } catch (IOException e) {
            throw new IllegalStateException("Invalid mapping CSV", e);
        }

        return cfg;
    }

    public CsvFieldMapping getOptional(String targetField) {
        return byTargetField.get(targetField); // darf null sein
    }

    public CsvFieldMapping get(String targetField) {
        return Optional.ofNullable(byTargetField.get(targetField))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Kein Mapping für Zielfeld: " + targetField
                        )
                );
    }
}