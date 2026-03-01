package com.kcserver.dto.person;

import com.kcserver.dto.mitglied.MitgliedDetailDTO;
import com.kcserver.enumtype.Sex;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PersonDetailDTO {

    private Long id;
    private String vorname;
    private String name;
    private Sex sex;
    private String email;
    private boolean aktiv;
    private LocalDate geburtsdatum;

    private String telefon;
    private String telefonFestnetz;
    private String strasse;
    private String plz;
    private String ort;
    private String countryCode;
    private String bankName;
    private String iban;
    private String bic;

    private String efz;

    @Size(max = 6, message = "Kürzel darf maximal 6 Zeichen lang sein")
    private String kuerzel;

    private List<MitgliedDetailDTO> mitgliedschaften;

    // getters / setters
}