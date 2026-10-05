package com.kcserver.kjfp.dto.simulation;

import com.kcserver.kjfp.enumtype.FinanzKategorie;
import lombok.*;
import java.math.BigDecimal;

@Builder
@Getter
@Setter
public class SimulationPosition {

    private FinanzKategorie kategorie;
    private BigDecimal betrag;
    private boolean automatisch;
}
