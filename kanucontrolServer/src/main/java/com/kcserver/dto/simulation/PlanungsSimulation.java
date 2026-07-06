package com.kcserver.dto.simulation;

import com.kcserver.entity.Beitragsstruktur;
import com.kcserver.enumtype.VeranstaltungTyp;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanungsSimulation {

    private LocalDate beginnDatum;

    private boolean kikZertifiziert;

    // Gebühren
    private Long beitragsstrukturId;
    private BigDecimal teilnehmerBeitragUnter21Jahre;
    private BigDecimal mitarbeiterBeitrag;

    // Personen (Eingabewerte)
    private int teilnehmer;
    private int mitarbeiter;

    // Dauer
    private long tage;
    private long naechte;

    // Preise
    private BigDecimal unterkunftPreisProPersonUndNacht;
    private BigDecimal verpflegungPreisProPersonUndTag;

    // Kosten (Eingabewerte)
    private BigDecimal honorare;
    private BigDecimal fahrtkosten;
    private BigDecimal verbrauchsmaterialProTag;
    private BigDecimal kultur;
    private BigDecimal miete;
    private BigDecimal sonstigeKostenProTag;

    // Einnahmen (Eingabewerte)
    private BigDecimal pfand;
    private BigDecimal sonstigeEinnahmenProTag;

    // Förderung
    private VeranstaltungTyp typ;
}
