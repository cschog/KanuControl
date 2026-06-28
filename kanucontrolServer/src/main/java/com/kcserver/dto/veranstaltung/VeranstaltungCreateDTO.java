package com.kcserver.dto.veranstaltung;

import com.kcserver.enumtype.VeranstaltungTyp;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class VeranstaltungCreateDTO {

    @NotNull
    private String name;

    @NotNull
    private VeranstaltungTyp typ;

    @NotNull
    private Long vereinId;

    @NotNull
    private Long leiterId;

    @NotNull
    private LocalDate beginnDatum;

    @NotNull
    private LocalDate endeDatum;

    @NotNull
    private LocalTime beginnZeit;

    @NotNull
    private LocalTime endeZeit;
}