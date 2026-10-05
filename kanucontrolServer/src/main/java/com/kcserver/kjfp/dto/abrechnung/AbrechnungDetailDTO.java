package com.kcserver.kjfp.dto.abrechnung;

import com.kcserver.kjfp.dto.finanzen.FinanzSummaryDTO;
import com.kcserver.kjfp.enumtype.AbrechnungsStatus;
import lombok.Data;

import java.util.List;
import java.math.BigDecimal;

@Data
public class AbrechnungDetailDTO {

    private Long veranstaltungId;
    private AbrechnungsStatus status;

    private List<AbrechnungBelegDTO> belege;

    private FinanzSummaryDTO finanz;

    private BigDecimal verwendeterFoerdersatz;   // ✅ neu
}