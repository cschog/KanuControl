package com.kcserver.dto.simulation;

import lombok.*;
import java.math.BigDecimal;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanungsSimulation {

    private VeranstaltungsInfo veranstaltung;

    // Gebühren
    private Long beitragsstrukturId;
    private BigDecimal teilnehmerBeitragUnter21Jahre;
    private BigDecimal mitarbeiterBeitrag;

    // Personen
    private int teilnehmer;
    private int mitarbeiter;

    // Preise
    private BigDecimal unterkunftPreisProPersonUndNacht;
    private BigDecimal verpflegungPreisProPersonUndTag;

    // Kosten
    private BigDecimal honorare;
    private BigDecimal fahrtkosten;
    private BigDecimal verbrauchsmaterialProTag;
    private BigDecimal kultur;
    private BigDecimal miete;
    private BigDecimal sonstigeKostenProTag;

    // Einnahmen
    private BigDecimal pfand;
}
