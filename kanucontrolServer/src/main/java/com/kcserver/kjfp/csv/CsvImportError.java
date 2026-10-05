package com.kcserver.kjfp.csv;

public record CsvImportError(
        int row,
        String field,
        String value,
        String message
) {}