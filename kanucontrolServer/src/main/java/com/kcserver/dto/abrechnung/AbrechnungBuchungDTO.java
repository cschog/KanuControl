package com.kcserver.dto.abrechnung;

import com.kcserver.enumtype.FinanzKategorie;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AbrechnungBuchungDTO {

    private Long id;

    private FinanzKategorie kategorie;

    private BigDecimal betrag;

    private LocalDate datum;

    private Long personId;

    private String kuerzel;   // derived
    private String beschreibung;
}