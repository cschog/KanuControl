package com.kcserver.kjfp.dto.planung;

import com.kcserver.kjfp.enumtype.FinanzKategorie;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlanungPositionDTO {

    private Long id;
    private FinanzKategorie kategorie;
    private BigDecimal betrag;
    private String kommentar;
    private BigDecimal menge;

    private String einheit;
    private BigDecimal einzelpreis;
    private boolean automatischBerechnet;
    private boolean editierbar;
}
