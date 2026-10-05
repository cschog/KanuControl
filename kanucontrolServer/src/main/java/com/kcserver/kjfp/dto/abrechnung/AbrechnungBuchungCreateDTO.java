package com.kcserver.kjfp.dto.abrechnung;

import com.kcserver.kjfp.enumtype.FinanzKategorie;
import com.kcserver.kjfp.enumtype.Zahlungsweg;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AbrechnungBuchungCreateDTO {

    @NotNull
    private FinanzKategorie kategorie;

    @NotNull
    private BigDecimal betrag;
    private String beschreibung;

    private Zahlungsweg zahlungsweg;
}