package com.kcserver.csv;

public record CsvImportError(
        int row,
        String message
) {}