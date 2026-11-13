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

    private String programmentwurfTitel;
    private String programmentwurfInhalt;

    /** Nur beim Kopieren: Quelle im aktuellen Mandanten. Planung wird serverseitig kopiert. */
    @jakarta.validation.constraints.Positive
    private Long kopieVonVeranstaltungId;

    private com.kcserver.kjfp.enumtype.CountryCode countryCode;
    private String plz;
    private String ort;
    private Long unterkunftsartId;
    private Long verpflegungsmodellId;
    private Long beitragsstrukturId;
    private Boolean individuelleGebuehren;
    private java.math.BigDecimal standardGebuehr;
    private com.kcserver.core.enumtype.VeranstaltungScope scope;

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
