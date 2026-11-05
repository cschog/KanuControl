package com.kcserver.core.dto.veranstaltung;

import com.kcserver.kjfp.enumtype.CountryCode;
import com.kcserver.core.enumtype.VeranstaltungScope;
import com.kcserver.kjfp.enumtype.VeranstaltungTyp;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class VeranstaltungUpdateDTO {

    private String name;
    private VeranstaltungTyp typ;

    private Boolean kjfpAktiv;
    private Boolean fahrtenAktiv;

    private LocalDate beginnDatum;
    private LocalTime beginnZeit;

    private LocalDate endeDatum;

    private String beschreibung;
    private LocalTime endeZeit;

    private Boolean individuelleGebuehren;
    private BigDecimal standardGebuehr;

    private Long beitragsstrukturId;

    private VeranstaltungScope scope;

    // ⭐ NEU
    private Long leiterId;
    private Long vereinId;

    /* ================= Detailfelder ================= */

    private CountryCode countryCode;
    private String plz;
    private String ort;

    private Long unterkunftsartId;
    private Long verpflegungsmodellId;
}
