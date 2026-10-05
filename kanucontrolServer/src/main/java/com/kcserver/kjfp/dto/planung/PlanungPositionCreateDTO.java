package com.kcserver.kjfp.dto.planung;

import com.kcserver.kjfp.enumtype.FinanzKategorie;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlanungPositionCreateDTO {

    private FinanzKategorie kategorie;
    private BigDecimal betrag;
    private String kommentar;
}