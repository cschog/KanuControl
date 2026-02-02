package com.kcserver.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.Reader;
import java.util.List;

public final class CsvReader {

    private CsvReader() {}

    public static List<CSVRecord> read(Reader reader) {

        try {
            CSVParser parser = CSVFormat.Builder.create()
                    .setDelimiter(';')
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .setIgnoreEmptyLines(true)
                    .build()
                    .parse(reader);

            return parser.getRecords();

        } catch (Exception e) {
            throw new IllegalStateException("CSV konnte nicht gelesen werden", e);
        }
    }
}