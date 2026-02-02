package com.kcserver.csv;

import java.util.stream.Collectors;

public final class CsvMappingTemplateGenerator {

    private CsvMappingTemplateGenerator() {}

    public static String generate() {

        String header = "csv_column;target_field;converter";

        String body = CsvPersonImportSchema.fields().stream()
                .map(f ->
                        f.exampleCsvColumn() + ";" +
                                f.targetField() + ";" +
                                (f.defaultConverter() == null ? "" : f.defaultConverter())
                )
                .collect(Collectors.joining("\n"));

        return header + "\n" + body + "\n";
    }
}