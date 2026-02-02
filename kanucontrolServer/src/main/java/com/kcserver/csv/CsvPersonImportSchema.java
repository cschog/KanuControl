package com.kcserver.csv;

import java.util.List;

public final class CsvPersonImportSchema {

    private CsvPersonImportSchema() {}

    public static List<CsvFieldSpec> fields() {
        return List.of(
                new CsvFieldSpec("Vorname", "vorname", false, null),
                new CsvFieldSpec("Nachname", "name", false, null),
                new CsvFieldSpec("Geschlecht", "sex", true, "sex_de"),
                new CsvFieldSpec("Geburtsdatum", "geburtsdatum", true, "date_de"),
                new CsvFieldSpec("Telefon Mobil", "telefon", true, null),
                new CsvFieldSpec("Telefon Privat", "telefonFestnetz", true, null),
                new CsvFieldSpec("E-Mail", "email", true, null),
                new CsvFieldSpec("Adresse", "strasse", true, null),
                new CsvFieldSpec("PLZ", "plz", true, null),
                new CsvFieldSpec("Ort", "ort", true, null)
        );
    }
}