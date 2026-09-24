package com.kcserver.csv;

import com.kcserver.exception.CsvReadException;
import com.kcserver.exception.ErrorMessages;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

public final class CsvReader {

    private static final char[] SUPPORTED_DELIMITERS = {
            ';',
            ',',
            '\t'
    };

    private CsvReader() {
    }

    public static List<CSVRecord> read(Reader reader) {

        final String csvContent;

        try {
            csvContent = removeBom(readAll(reader));
        } catch (IOException e) {
            throw new CsvReadException(
                    ErrorMessages.CSV_NOT_READABLE,
                    e
            );
        }

        if (csvContent.isBlank()) {
            throw new IllegalArgumentException(
                    ErrorMessages.CSV_EMPTY
            );
        }

        final char delimiter;

        try {
            delimiter = detectDelimiter(csvContent);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    ErrorMessages.CSV_DELIMITER_NOT_DETECTED,
                    e
            );
        }

        try (
                CSVParser parser = CSVFormat.Builder.create()
                        .setDelimiter(delimiter)
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setTrim(true)
                        .setIgnoreEmptyLines(true)
                        .build()
                        .parse(new StringReader(csvContent))
        ) {

            return parser.getRecords();

        } catch (IOException e) {

            throw new CsvReadException(
                    ErrorMessages.CSV_INVALID,
                    e
            );
        }
    }

    private static String removeBom(String content) {

        if (content != null && content.startsWith("\uFEFF")) {
            return content.substring(1);
        }

        return content;
    }

    private static char detectDelimiter(String csvContent) {

        String headerLine = findFirstNonEmptyLine(csvContent);

        if (headerLine == null) {
            throw new IllegalArgumentException(
                    ErrorMessages.CSV_NO_DATA
            );
        }

        char bestDelimiter = 0;
        int highestCount = 0;

        for (char delimiter : SUPPORTED_DELIMITERS) {

            int count = countDelimiterOutsideQuotes(
                    headerLine,
                    delimiter
            );

            if (count > highestCount) {
                highestCount = count;
                bestDelimiter = delimiter;
            }
        }

        if (bestDelimiter == 0 || highestCount == 0) {
            throw new IllegalArgumentException(
                    ErrorMessages.CSV_DELIMITER_NOT_DETECTED
            );
        }

        return bestDelimiter;
    }

    private static String findFirstNonEmptyLine(String content) {

        String[] lines = content.split("\\R");

        for (String line : lines) {
            if (!line.isBlank()) {
                return line;
            }
        }

        return null;
    }

    private static int countDelimiterOutsideQuotes(
            String text,
            char delimiter
    ) {

        boolean insideQuotes = false;
        int count = 0;

        for (int i = 0; i < text.length(); i++) {

            char current = text.charAt(i);

            if (current == '"') {

                /*
                 * Zwei aufeinanderfolgende Anführungszeichen
                 * innerhalb eines CSV-Feldes bedeuten ein
                 * escaped Quote.
                 */
                if (
                        insideQuotes
                                && i + 1 < text.length()
                                && text.charAt(i + 1) == '"'
                ) {
                    i++;
                    continue;
                }

                insideQuotes = !insideQuotes;
                continue;
            }

            if (!insideQuotes && current == delimiter) {
                count++;
            }
        }

        return count;
    }

    private static String readAll(Reader reader) throws IOException {

        StringBuilder content = new StringBuilder();

        char[] buffer = new char[4096];

        int length;

        while ((length = reader.read(buffer)) != -1) {
            content.append(buffer, 0, length);
        }

        return content.toString();
    }
}