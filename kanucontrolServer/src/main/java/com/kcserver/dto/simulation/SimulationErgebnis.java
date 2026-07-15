package com.kcserver.dto.simulation;

import com.kcserver.dto.beitrag.BeitragsVorschlag;
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

    private BigDecimal summeTeilnehmerbeitraege;

    private BigDecimal durchschnittlicherPersonenbeitrag;

    private BigDecimal empfohlenerPersonenbeitrag;

    private BigDecimal teilnehmerBeitrag;
    private BigDecimal mitarbeiterBeitrag;

    private BeitragsVorschlag beitragsVorschlag;
}