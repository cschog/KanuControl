package com.kcserver.dto.simulation;

import com.kcserver.enumtype.VeranstaltungTyp;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VeranstaltungsInfo {

    private Long id;
    private String name;
    private LocalDate beginnDatum;
    private LocalDate endeDatum;
    private VeranstaltungTyp typ;
    private long tage;
    private long naechte;

    // Verein
    private boolean vereinKikZertifiziert;

    private String beitragsstrukturName;
    private String unterkunftsartName;
    private String verpflegungsmodellName;
}
