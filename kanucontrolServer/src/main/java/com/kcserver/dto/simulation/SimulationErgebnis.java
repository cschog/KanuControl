package com.kcserver.dto.simulation;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Builder
@Getter
@Setter
public class SimulationErgebnis {

    private List<SimulationPosition> positionen;

    private BigDecimal kosten;
    private BigDecimal einnahmen;
    private BigDecimal saldo;
}