package com.kcserver.core.dto.veranstaltung;

import com.kcserver.core.dto.person.PersonDetailDTO;
import com.kcserver.kjfp.dto.unterkunft.UnterkunftsartRefDTO;
import com.kcserver.core.dto.verein.VereinRefDTO;
import com.kcserver.kjfp.dto.verpflegung.VerpflegungsmodellRefDTO;
import com.kcserver.kjfp.enumtype.CountryCode;
import com.kcserver.core.enumtype.VeranstaltungScope;
import com.kcserver.kjfp.enumtype.VeranstaltungTyp;
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

    private UnterkunftsartRefDTO unterkunftsart;
    private VerpflegungsmodellRefDTO verpflegungsmodell;

    private String plz;
    private String ort;
    private CountryCode countryCode;

    private LocalDate beginnDatum;
    private LocalTime beginnZeit;

    private LocalDate endeDatum;

    private String beschreibung;
    private LocalTime endeZeit;

    private Boolean individuelleGebuehren;
    private BigDecimal standardGebuehr;

    private Long beitragsstrukturId;
    private String beitragsstrukturName;

    private VeranstaltungScope scope;

    /* =========================
       Beziehungen
       ========================= */

    private Long vereinId;
    private Long leiterId;

    private VereinRefDTO verein;
    private PersonDetailDTO leiter;

    /* =========================
       Status
       ========================= */

    private Boolean aktiv;
    private boolean kjfpAktiv;
    private boolean fahrtenAktiv;
}
