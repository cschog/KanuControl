package com.kcserver.dto.planung;

import com.kcserver.dto.finanzen.FinanzSummaryDTO;
import com.kcserver.enumtype.PlanungsStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlanungDetailDTO {

    private Long id;

    private Long veranstaltungId;

    private PlanungsStatus status;

    private List<PlanungPositionDTO> positionen;

    private FinanzSummaryDTO finanz;

}