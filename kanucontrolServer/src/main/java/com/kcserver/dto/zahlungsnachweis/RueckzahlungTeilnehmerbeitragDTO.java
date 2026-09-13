package com.kcserver.dto.zahlungsnachweis;

import com.kcserver.enumtype.Zahlungsweg;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RueckzahlungTeilnehmerbeitragDTO {

    @NotNull
    private Long zahlungsnachweisId;

    @NotNull
    @DecimalMin(
            value = "0.01",
            message = "Der Rückzahlungsbetrag muss mindestens 0,01 betragen."
    )
    private BigDecimal betrag;
    private String beschreibung;
    private Zahlungsweg zahlungsweg;
}