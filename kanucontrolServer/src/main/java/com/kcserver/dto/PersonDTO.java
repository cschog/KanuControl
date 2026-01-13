package com.kcserver.dto;

import com.kcserver.enumtype.Sex;
import com.kcserver.validation.ExactlyOneHauptverein;
import com.kcserver.validation.IbanRequiresBankName;
import com.kcserver.validation.OnCreate;
import com.kcserver.validation.OnUpdate;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ExactlyOneHauptverein(groups = {OnCreate.class, OnUpdate.class})
@IbanRequiresBankName(groups = {OnCreate.class, OnUpdate.class})
public class PersonDTO {

    @Null(groups = OnCreate.class, message = "ID must be null when creating")
    @NotNull(groups = OnUpdate.class, message = "ID is required for update")
    private Long id;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Size(min = 2, max = 100)
    private String vorname;

    @PastOrPresent(
            groups = {OnCreate.class, OnUpdate.class},
            message = "Geburtsdatum darf nicht in der Zukunft liegen"
    )
    private LocalDate geburtsdatum;

    /** M / W / D */
    @NotNull(groups = OnCreate.class)
    private Sex sex;

    private String telefonFestnetz;
    private String strasse;
    private String plz;
    private String ort;

    @Pattern(
            regexp = "^[0-9+\\-/ ]*$",
            message = "Telefon contains invalid characters"
    )
    private String telefon;

    private String bankName;
    private String iban;

    private Boolean aktiv;

    private List<MitgliedDTO> mitgliedschaften;

    /* =========================================================
       🔧 HARTE ABSICHERUNG – expliziter Getter
       ========================================================= */

    public List<MitgliedDTO> getMitgliedschaften() {
        return mitgliedschaften;
    }
}