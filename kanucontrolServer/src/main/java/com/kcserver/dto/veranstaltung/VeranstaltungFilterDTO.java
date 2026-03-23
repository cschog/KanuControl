package com.kcserver.dto.veranstaltung;

import com.kcserver.enumtype.VeranstaltungTyp;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VeranstaltungFilterDTO {

    private String name;
    private Boolean aktiv;
    private Long vereinId;
    private LocalDate beginnDatum;
    private LocalDate endeDatum;
    private VeranstaltungTyp typ;
}