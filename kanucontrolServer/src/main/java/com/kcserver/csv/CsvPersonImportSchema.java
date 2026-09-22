package com.kcserver.csv;

import java.util.List;

public final class CsvPersonImportSchema {

    private CsvPersonImportSchema() {}

    public static List<CsvFieldSpec> fields() {
        return List.of(
                new CsvFieldSpec("Vorname", "vorname", false, null),
                new CsvFieldSpec("Nachname", "name", false, null),
                new CsvFieldSpec("Geschlecht", "sex", false, "sex_de"),
                new CsvFieldSpec("Geburtsdatum", "geburtsdatum", true, "date_de"),

                new CsvFieldSpec("PLZ", "plz", true, null),
                new CsvFieldSpec("Ort", "ort", true, null),
                new CsvFieldSpec("Strasse", "strasse", true, null),
                new CsvFieldSpec("Land", "countryCode", true, null),

                new CsvFieldSpec("Telefon Festnetz", "telefonFestnetz", true, null),
                new CsvFieldSpec("Telefon", "telefon", true, null),
                new CsvFieldSpec("Email", "email", true, null),

                new CsvFieldSpec("Bankname", "bankName", true, null),
                new CsvFieldSpec("IBAN", "iban", true, "iban"),
                new CsvFieldSpec("BIC", "bic", true, "bic"),
                new CsvFieldSpec("eFZ", "efz", true, "date_de"),

                new CsvFieldSpec("Aktiv", "aktiv", true, "bool_ja_nein")
        );
    }
}