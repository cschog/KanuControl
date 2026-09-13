package com.kcserver.dto.abrechnung;

import com.kcserver.enumtype.BuchungsHerkunft;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.enumtype.Zahlungsweg;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AbrechnungBuchungDTO {

    private Long id;
    private BuchungsHerkunft herkunft;
    private FinanzKategorie kategorie;
    private BigDecimal betrag;
    private String beschreibung;
    private Zahlungsweg zahlungsweg;
}