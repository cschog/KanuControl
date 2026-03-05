package com.kcserver.dto.planung;

import com.kcserver.enumtype.FinanzKategorie;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlanungPositionDTO {

    private Long id;
    private FinanzKategorie kategorie;
    private BigDecimal betrag;
    private String kommentar;
}
