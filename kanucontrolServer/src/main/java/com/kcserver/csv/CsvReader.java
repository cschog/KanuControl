package com.kcserver.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;

public final class CsvReader {

    private CsvReader() {}

    public static List<CSVRecord> read(InputStream input) {

        try (Reader reader = CsvEncoding.reader(input)) {

            CSVParser parser = CSVFormat.Builder.create()
                    .setDelimiter(';')              // Clubdesk
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            return parser.getRecords();

        } catch (Exception e) {
            throw new IllegalStateException("CSV konnte nicht gelesen werden", e);
        }
    }
}