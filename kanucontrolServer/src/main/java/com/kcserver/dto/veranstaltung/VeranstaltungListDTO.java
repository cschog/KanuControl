package com.kcserver.dto.veranstaltung;

import com.kcserver.enumtype.VeranstaltungTyp;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VeranstaltungListDTO {

    private Long id;
    private String name;
    private VeranstaltungTyp typ;

    private LocalDate beginnDatum;
    private LocalDate endeDatum;

    private boolean aktiv;

    // Anzeige
    private String vereinName;
    private String leiterName;
}