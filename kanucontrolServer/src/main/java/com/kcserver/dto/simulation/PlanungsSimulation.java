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

    /**
     * Soll der KiK-Zuschlag berücksichtigt werden?
     */
    private boolean kikZertifiziert;

    /* Teilnehmer */

    private int teilnehmer;
    private int mitarbeiter;

    /* Beiträge */

    private BigDecimal teilnehmerBeitragUnter21Jahre;
    private BigDecimal mitarbeiterBeitrag;

    /* Preise */

    private BigDecimal unterkunftPreisProPersonUndNacht;
    private BigDecimal verpflegungPreisProPersonUndTag;

    /* Kosten */

    private BigDecimal honorare;
    private BigDecimal fahrtkosten;
    private BigDecimal verbrauchsmaterialProTag;
    private BigDecimal kultur;
    private BigDecimal miete;
    private BigDecimal sonstigeKostenProTag;
}
