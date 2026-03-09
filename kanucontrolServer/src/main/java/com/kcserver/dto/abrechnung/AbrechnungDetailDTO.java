package com.kcserver.dto.abrechnung;

import com.kcserver.dto.finanz.FinanzSummaryDTO;
import com.kcserver.enumtype.AbrechnungsStatus;
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