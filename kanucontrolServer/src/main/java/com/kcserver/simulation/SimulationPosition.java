package com.kcserver.simulation;

import com.kcserver.enumtype.FinanzKategorie;
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
