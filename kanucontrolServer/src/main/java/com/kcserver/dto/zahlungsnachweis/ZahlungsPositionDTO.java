package com.kcserver.dto.zahlungsnachweis;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ZahlungsPositionDTO {

    private Long id;

    @NotNull
    private Long teilnehmerId;

    private String vorname;

    private String nachname;

    @NotNull
    @PositiveOrZero
    private BigDecimal betrag;
}