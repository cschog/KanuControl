package com.kcserver.csv;

import com.kcserver.entity.Person;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class CsvPersonExporter {

    private static final DateTimeFormatter DATE_DE =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private CsvPersonExporter() {
    }

    public static byte[] export(List<Person> personen) {

        StringWriter writer = new StringWriter();

        try (CSVPrinter printer = new CSVPrinter(
                writer,
                CSVFormat.Builder.create()
                        .setDelimiter(';')
                        .setQuote('"')
                        .setRecordSeparator("\r\n")
                        .build()
        )) {

            // Header
            printer.printRecord(
                    CsvPersonImportSchema.fields()
                            .stream()
                            .map(CsvFieldSpec::exampleCsvColumn)
                            .toList()
            );

            // Daten
            for (Person person : personen) {

                List<Object> values = new java.util.ArrayList<>();

                for (CsvFieldSpec field : CsvPersonImportSchema.fields()) {
                    values.add(
                            formatValue(
                                    getValue(person, field.targetField())
                            )
                    );
                }

                printer.printRecord(values);
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "CSV-Export konnte nicht erstellt werden",
                    e
            );
        }

        /*
         * UTF-8 BOM:
         * Damit erkennt Excel die Datei zuverlässig als UTF-8.
         */
        return ("\uFEFF" + writer)
                .getBytes(StandardCharsets.UTF_8);
    }

    private static Object getValue(
            Person person,
            String targetField
    ) {
        return switch (targetField) {

            case "vorname" ->
                    person.getVorname();

            case "name" ->
                    person.getName();

            case "sex" ->
                    person.getSex();

            case "geburtsdatum" ->
                    person.getGeburtsdatum();

            case "plz" ->
                    person.getPlz();

            case "ort" ->
                    person.getOrt();

            case "strasse" ->
                    person.getStrasse();

            case "countryCode" ->
                    person.getCountryCode();

            case "telefonFestnetz" ->
                    person.getTelefonFestnetz();

            case "telefon" ->
                    person.getTelefon();

            case "email" ->
                    person.getEmail();

            case "bankName" ->
                    person.getBankName();

            case "iban" ->
                    person.getIban();

            case "bic" ->
                    person.getBic();

            case "efz" ->
                    person.getEfz();

            case "aktiv" ->
                    person.isAktiv();

            default ->
                    throw new IllegalArgumentException(
                            "Unbekanntes CSV-Exportfeld: "
                                    + targetField
                    );
        };
    }

    private static String formatValue(Object value) {

        if (value == null) {
            return "";
        }

        if (value instanceof LocalDate date) {
            return date.format(DATE_DE);
        }

        return switch (value) {
            case com.kcserver.enumtype.Sex sex ->
                    formatSex(sex);

            case Boolean bool ->
                    bool.toString();

            default ->
                    value.toString();
        };
    }

    private static String formatSex(
            com.kcserver.enumtype.Sex sex
    ) {
        return switch (sex) {
            case MAENNLICH -> "M";
            case WEIBLICH -> "W";
            case DIVERS -> "D";
        };
    }
}