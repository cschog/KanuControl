package com.kcserver.dto.abrechnung;

import com.kcserver.enumtype.FinanzKategorie;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AbrechnungBuchungCreateDTO {

    @NotNull
    private Long teilnehmerId;

    @NotBlank
    private String kuerzel;

    @NotNull
    private FinanzKategorie kategorie;

    @NotNull
    private BigDecimal betrag;

    @NotNull
    private LocalDate datum;

    private String beschreibung;
}