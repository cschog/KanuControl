package com.kcserver.kjfp.dto.abrechnung;

import com.kcserver.kjfp.enumtype.BuchungsHerkunft;
import com.kcserver.kjfp.enumtype.FinanzKategorie;
import com.kcserver.kjfp.enumtype.Zahlungsweg;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AbrechnungBuchungDTO {

    private Long id;
    private BuchungsHerkunft herkunft;
    private FinanzKategorie kategorie;
    private BigDecimal betrag;
    private String beschreibung;
    private Zahlungsweg zahlungsweg;
}