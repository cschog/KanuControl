package com.kcserver.csv;

import com.kcserver.dto.person.PersonSaveDTO;
import com.kcserver.enumtype.Sex;

import java.time.LocalDate;

public final class CsvPersonImporter {

    private CsvPersonImporter() {
    }

    public static PersonSaveDTO toPersonSaveDTO(
            CsvPersonRow row,
            CsvMappingConfig cfg
    ) {
        PersonSaveDTO dto = new PersonSaveDTO();

        // Pflichtfelder
        dto.setVorname((String) row.get(cfg.get("vorname")));
        dto.setName((String) row.get(cfg.get("name")));
        dto.setSex((Sex) row.get(cfg.get("sex")));

        // Optionale Felder
        set(row, cfg, "geburtsdatum", v -> dto.setGeburtsdatum((LocalDate) v));
        set(row, cfg, "plz", v -> dto.setPlz((String) v));
        set(row, cfg, "ort", v -> dto.setOrt((String) v));
        set(row, cfg, "strasse", v -> dto.setStrasse((String) v));
        set(row, cfg, "countryCode", v -> dto.setCountryCode((String) v));
        set(row, cfg, "telefonFestnetz", v -> dto.setTelefonFestnetz((String) v));
        set(row, cfg, "telefon", v -> dto.setTelefon((String) v));
        set(row, cfg, "email", v -> dto.setEmail((String) v));
        set(row, cfg, "bankName", v -> dto.setBankName((String) v));
        set(row, cfg, "iban", v -> dto.setIban((String) v));
        set(row, cfg, "bic", v -> dto.setBic((String) v));
        set(row, cfg, "efz", v -> dto.setEfz((LocalDate) v));
        set(row, cfg, "aktiv", v -> dto.setAktiv((Boolean) v));

        return dto;
    }

    private static void set(
            CsvPersonRow row,
            CsvMappingConfig cfg,
            String targetField,
            java.util.function.Consumer<Object> setter
    ) {
        CsvFieldMapping mapping = cfg.getOptional(targetField);

        if (mapping == null) {
            return;
        }

        Object value = row.get(mapping);

        if (value != null) {
            setter.accept(value);
        }
    }
}