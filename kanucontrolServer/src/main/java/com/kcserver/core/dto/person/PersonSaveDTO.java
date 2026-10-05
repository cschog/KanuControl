package com.kcserver.core.dto.person;

import com.kcserver.core.dto.mitglied.HasMitgliedschaften;
import com.kcserver.core.dto.mitglied.MitgliedSaveDTO;
import com.kcserver.kjfp.enumtype.Sex;
import com.kcserver.kjfp.validation.ExactlyOneHauptverein;
import com.kcserver.core.validation.OnCreate;
import com.kcserver.core.validation.OnUpdate;
import com.kcserver.core.validation.ValidIban;
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
public class PersonSaveDTO

        implements HasMitgliedschaften<MitgliedSaveDTO> {

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

    private Boolean aktiv;

    @Valid
    private List<MitgliedSaveDTO> mitgliedschaften;

    @Override
    public List<MitgliedSaveDTO> getMitgliedschaften() {
        return mitgliedschaften;
    }
}