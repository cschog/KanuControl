package com.kcserver.dto.veranstaltung;

import com.kcserver.enumtype.VeranstaltungTyp;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class VeranstaltungUpdateDTO {

    private String name;
    private VeranstaltungTyp typ;

    private LocalDate beginnDatum;
    private LocalTime beginnZeit;

    private LocalDate endeDatum;
    private LocalTime endeZeit;

    // ⭐ NEU
    private Long leiterId;
    private Long vereinId;
}