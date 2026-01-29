package com.kcserver.dto;

import com.kcserver.enumtype.Sex;
import com.kcserver.validation.ExactlyOneHauptverein;
import com.kcserver.validation.IbanRequiresBankName;
import com.kcserver.validation.OnCreate;
import com.kcserver.validation.OnUpdate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IbanRequiresBankName(groups = {OnCreate.class, OnUpdate.class})
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
    private String iban;

    private Boolean aktiv;

    @Valid
    private List<MitgliedSaveDTO> mitgliedschaften;

    @Override
    public List<MitgliedSaveDTO> getMitgliedschaften() {
        return mitgliedschaften;
    }
}