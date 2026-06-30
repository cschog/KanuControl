package com.kcserver.dto.planung;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
public class PlanungsSimulation {

    private Long veranstaltungId;

    // Teilnehmer
    private int teilnehmer;
    private int mitarbeiter;

    // Gebühren
    private BigDecimal standardGebuehr;

    private Long beitragsstrukturId;

    // Unterkunft
    private Long unterkunftsartId;

    // Verpflegung
    private Long verpflegungsmodellId;

    // später beliebig erweiterbar
}
