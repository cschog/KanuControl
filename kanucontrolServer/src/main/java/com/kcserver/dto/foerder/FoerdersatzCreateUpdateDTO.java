package com.kcserver.dto.foerder;

import com.kcserver.enumtype.VeranstaltungTyp;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class FoerdersatzCreateUpdateDTO {

    @NotNull(message = "gültig von ist erforderlich")
    private LocalDate gueltigVon;

    private LocalDate gueltigBis;

    @NotNull(message = "Typ ist erforderlich")
    private VeranstaltungTyp typ;

    @NotNull(message = "Fördersatz ist erforderlich")
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal foerdersatz;

    @Size(max = 255)
    private String beschluss;
}