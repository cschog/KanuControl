package com.kcserver.dto.planung;

import com.kcserver.dto.finanz.FinanzSummaryDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlanungDetailDTO {

    private Long id;

    private Long veranstaltungId;

    private boolean eingereicht;

    private List<PlanungPositionDTO> positionen;

    private FinanzSummaryDTO finanz;

}