package com.kcserver.dto.person;

import com.kcserver.dto.mitglied.MitgliedDTO;
import com.kcserver.enumtype.CountryCode;
import com.kcserver.enumtype.Sex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonDTO {

    private Long id;
    private String name;
    private String vorname;
    private LocalDate geburtsdatum;

    // READ ONLY
    private Integer alter;

    private Sex sex;
    private String telefonFestnetz;
    private String strasse;
    private String plz;
    private String ort;
    private CountryCode countryCode;
    private String telefon;
    private String bankName;
    private String iban;
    private Boolean aktiv;

    // READ ONLY
    private List<MitgliedDTO> mitgliedschaften;
}