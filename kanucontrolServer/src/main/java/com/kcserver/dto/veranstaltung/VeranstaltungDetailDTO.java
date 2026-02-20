package com.kcserver.dto.veranstaltung;

import com.kcserver.dto.person.PersonDetailDTO;
import com.kcserver.dto.verein.VereinDTO;
import com.kcserver.enumtype.CountryCode;
import com.kcserver.enumtype.VeranstaltungTyp;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class VeranstaltungDetailDTO {

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

    private Boolean individuelleGebuehren;
    private BigDecimal standardGebuehr;

    /* =========================
       Beziehungen
       ========================= */

    private Long vereinId;
    private Long leiterId;

    // optional für Anzeige
    private VereinDTO verein;
    private PersonDetailDTO leiter;

    /* =========================
       Plan-Zahlen
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