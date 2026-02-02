package com.kcserver.csv;

public record CsvFieldSpec(
        String exampleCsvColumn,
        String targetField,
        boolean optional,
        String defaultConverter
) {}