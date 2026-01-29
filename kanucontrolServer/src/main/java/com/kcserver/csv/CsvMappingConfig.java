package com.kcserver.csv;

import org.apache.commons.csv.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CsvMappingConfig {

    private final Map<String, CsvFieldMapping> byTargetField = new HashMap<>();

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