package com.kcserver.dto.unterkunft;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UnterkunftsartCreateUpdateDTO {

    @NotBlank
    private String bezeichnung;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal preisProPersonUndNacht;

    private String bemerkung;

    @NotNull
    private Boolean aktiv;
}