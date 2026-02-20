package com.kcserver.dto.veranstaltung;

import com.kcserver.enumtype.CountryCode;
import com.kcserver.enumtype.VeranstaltungTyp;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
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
    private LocalTime beginnZeit;

    @NotNull
    private LocalDate endeDatum;

    @NotNull
    private LocalTime endeZeit;

    private CountryCode laenderCode;

    private String plz;
    private String ort;

    private Boolean individuelleGebuehren;
    private BigDecimal standardGebuehr;

    private String artDerUnterkunft;
    private String artDerVerpflegung;

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