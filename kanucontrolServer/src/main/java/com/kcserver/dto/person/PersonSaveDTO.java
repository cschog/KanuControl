package com.kcserver.dto.person;

import com.kcserver.dto.mitglied.HasMitgliedschaften;
import com.kcserver.dto.mitglied.MitgliedSaveDTO;
import com.kcserver.enumtype.Sex;
import com.kcserver.validation.ExactlyOneHauptverein;
import com.kcserver.validation.OnCreate;
import com.kcserver.validation.OnUpdate;
import com.kcserver.validation.ValidIban;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ExactlyOneHauptverein(groups = OnCreate.class)
public class PersonSaveDTO implements HasMitgliedschaften {

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    private String name;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    private String vorname;

    @PastOrPresent(groups = {OnCreate.class, OnUpdate.class})
    private LocalDate geburtsdatum;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    private Sex sex;

    @Email(groups = {OnCreate.class, OnUpdate.class})
    private String email;

    private String telefon;
    private String telefonFestnetz;

    private String strasse;
    private String plz;
    private String ort;
    private String countryCode;

    private String bankName;

    @ValidIban
    private String iban;
    private String bic;

    private LocalDate efz;

    @Size(max = 6, message = "Kürzel darf maximal 6 Zeichen lang sein")
    private String kuerzel;

    private Boolean aktiv;

    @Valid
    private List<MitgliedSaveDTO> mitgliedschaften;

    @Override
    public List<MitgliedSaveDTO> getMitgliedschaften() {
        return mitgliedschaften;
    }
}