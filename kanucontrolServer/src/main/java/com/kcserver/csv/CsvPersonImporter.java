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

        dto.setVorname((String) row.get(cfg.get("vorname")));
        dto.setName((String) row.get(cfg.get("name")));
        dto.setOrt((String) row.get(cfg.get("ort")));
        dto.setPlz((String) row.get(cfg.get("plz")));
        dto.setStrasse((String) row.get(cfg.get("strasse")));

        dto.setSex((Sex) row.get(cfg.get("sex")));
        dto.setGeburtsdatum((LocalDate) row.get(cfg.get("geburtsdatum")));

        CsvFieldMapping emailMapping = cfg.getOptional("email");
        if (emailMapping != null) {
            dto.setEmail((String) row.get(emailMapping));
        }

        CsvFieldMapping telefonMapping = cfg.getOptional("telefon");
        if (telefonMapping != null) {
            dto.setTelefon((String) row.get(telefonMapping));
        }

        CsvFieldMapping telefonFestnetzMapping = cfg.getOptional("telefonFestnetz");
        if (telefonFestnetzMapping != null) {
            dto.setTelefonFestnetz((String) row.get(telefonFestnetzMapping));
        }

        // IBAN optional
        CsvFieldMapping ibanMapping = cfg.getOptional("iban");
        if (ibanMapping != null) {
            dto.setIban((String) row.get(ibanMapping));
        }

        // BIC optional
        CsvFieldMapping bicMapping = cfg.getOptional("bic");
        if (bicMapping != null) {
            dto.setBic((String) row.get(bicMapping));
        }

        // eFZ optional (Date!)
        CsvFieldMapping efzMapping = cfg.getOptional("efz");
        if (efzMapping != null) {
            dto.setEfz((LocalDate) row.get(efzMapping));
        }

        dto.setAktiv(true);
        return dto;
    }
}