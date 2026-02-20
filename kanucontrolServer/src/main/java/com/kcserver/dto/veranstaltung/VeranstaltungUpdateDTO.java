package com.kcserver.dto.veranstaltung;

import com.kcserver.enumtype.CountryCode;
import com.kcserver.enumtype.VeranstaltungTyp;
import lombok.Data;

import java.math.BigDecimal;
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

    private Boolean individuelleGebuehren;
    private BigDecimal standardGebuehr;

    // ⭐ NEU
    private Long leiterId;
    private Long vereinId;

    /* ================= Detailfelder ================= */

    private CountryCode laenderCode;
    private String plz;
    private String ort;

    private String artDerUnterkunft;
    private String artDerVerpflegung;

    private Integer geplanteTeilnehmerMaennlich;
    private Integer geplanteTeilnehmerWeiblich;
    private Integer geplanteTeilnehmerDivers;

    private Integer geplanteMitarbeiterMaennlich;
    private Integer geplanteMitarbeiterWeiblich;
    private Integer geplanteMitarbeiterDivers;
}