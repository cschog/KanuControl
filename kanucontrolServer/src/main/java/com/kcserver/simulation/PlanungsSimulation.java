package com.kcserver.simulation;

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

    // Personen
    private int teilnehmer;
    private int mitarbeiter;

    // Dauer
    private long tage;
    private long naechte;

    private LocalDate beginnDatum;

    private boolean kikZertifiziert;

    // Gebühren
    private BigDecimal standardGebuehr;
    private Beitragsstruktur beitragsstruktur;

    // Preise
    private BigDecimal unterkunftPreisProPersonUndNacht;
    private BigDecimal verpflegungPreisProPersonUndTag;

    // Förderung
    private VeranstaltungTyp typ;
}
