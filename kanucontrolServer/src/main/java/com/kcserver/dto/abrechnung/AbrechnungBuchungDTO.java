package com.kcserver.dto.abrechnung;

import com.kcserver.enumtype.FinanzKategorie;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AbrechnungBuchungDTO {

    private Long id;

    private Long teilnehmerId;
    private String kuerzel;

    private FinanzKategorie kategorie;
    private BigDecimal betrag;
    private LocalDate datum;
    private String beschreibung;
}