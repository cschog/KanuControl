package com.kcserver.kjfp.dto.planung;

import com.kcserver.kjfp.dto.finanzen.FinanzSummaryDTO;
import com.kcserver.kjfp.enumtype.PlanungsStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlanungDetailDTO {

    private Long id;

    private Long veranstaltungId;

    private PlanungsStatus status;

    private PlanungsSimulation simulation;

    private List<PlanungPositionDTO> positionen;

    private FinanzSummaryDTO finanz;
}