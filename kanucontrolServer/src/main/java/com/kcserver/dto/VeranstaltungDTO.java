package com.kcserver.dto;

import com.kcserver.enumtype.CountryCode;
import com.kcserver.enumtype.VeranstaltungTyp;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class VeranstaltungDTO {

    private Long id;

    /* =========================
       Stammdaten
       ========================= */

    private String name;
    private VeranstaltungTyp typ;

    private String artDerUnterkunft;
    private String artDerVerpflegung;

    private String plz;
    private String ort;

    private CountryCode laenderCode;

    private LocalDate beginnDatum;
    private LocalTime beginnZeit;

    private LocalDate endeDatum;
    private LocalTime endeZeit;

    /* =========================
       Beziehungen (IDs!)
       ========================= */

    private Long vereinId;
    private Long leiterId;

    /* =========================
       Plan-Zahlen (optional)
       ========================= */

    private Integer geplanteTeilnehmerMaennlich;
    private Integer geplanteTeilnehmerWeiblich;
    private Integer geplanteTeilnehmerDivers;

    private Integer geplanteMitarbeiterMaennlich;
    private Integer geplanteMitarbeiterWeiblich;
    private Integer geplanteMitarbeiterDivers;

    /* =========================
       Status
       ========================= */

    private Boolean aktiv;
}