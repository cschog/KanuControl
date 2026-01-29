package com.kcserver.csv;

import com.kcserver.dto.PersonSaveDTO;
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

        dto.setVorname((String) row.get(cfg.get("vorname")));
        dto.setName((String) row.get(cfg.get("name")));
        dto.setOrt((String) row.get(cfg.get("ort")));
        dto.setPlz((String) row.get(cfg.get("plz")));
        dto.setStrasse((String) row.get(cfg.get("strasse")));

        dto.setSex((Sex) row.get(cfg.get("sex")));
        dto.setGeburtsdatum((LocalDate) row.get(cfg.get("geburtsdatum")));

        // ✅ NEU: email optional
        CsvFieldMapping emailMapping = cfg.getOptional("email");
        if (emailMapping != null) {
            dto.setEmail((String) row.get(emailMapping));
        }

        dto.setAktiv(true);

        return dto;
    }

    /* =====================================================
       Helpers
       ===================================================== */

    private static String normalizeEmail(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }
}