package com.kcserver.dto.veranstaltung;

import com.kcserver.enumtype.VeranstaltungTyp;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;

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
    private LocalTime beginnZeit;

    @NotNull
    private LocalDate endeDatum;

    @NotNull
    private LocalTime endeZeit;

    private Boolean individuelleGebuehren;
    private BigDecimal standardGebuehr;

    /* =========================
       PLAN-ZAHLEN
       ========================= */

    private Integer geplanteTeilnehmerMaennlich;
    private Integer geplanteTeilnehmerWeiblich;
    private Integer geplanteTeilnehmerDivers;

    private Integer geplanteMitarbeiterMaennlich;
    private Integer geplanteMitarbeiterWeiblich;
    private Integer geplanteMitarbeiterDivers;
}