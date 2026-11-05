package com.kcserver.core.dto.veranstaltung;

import com.kcserver.kjfp.enumtype.VeranstaltungTyp;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class VeranstaltungCreateDTO {

    @NotNull
    private String name;

    private VeranstaltungTyp typ;

    @NotNull
    private Long vereinId;

    private Long leiterId;

    @NotNull
    private LocalDate beginnDatum;

    @NotNull
    private LocalDate endeDatum;

    private String beschreibung;

    private LocalTime beginnZeit;

    private LocalTime endeZeit;

    private Boolean kjfpAktiv = true;
    private Boolean fahrtenAktiv = false;
}
