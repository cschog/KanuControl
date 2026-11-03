package com.kcserver.vermietung.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class HomepageIntegrationDTO {
    private HomepageIntegrationDTO() {}

    public record Position(@NotNull @Positive Long mietbereichId, @NotNull @Min(1) @Max(10000) Integer anzahl) {}
    public record Auswahl(@NotNull LocalDate anreise, @NotNull LocalDate abreise,
            @NotEmpty @Size(max = 200) List<@NotNull @Valid Position> positionen) {}
    public record Anfrage(@NotNull java.util.UUID anfrageId, @NotNull Long konfigurationVersion,
            @NotNull @Valid Auswahl auswahl,
            @NotBlank @Size(max = 100) String vorname, @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 254) String email, @NotBlank @Size(max = 80) String telefon,
            @NotBlank @Size(max = 200) String strasse, @NotBlank @Size(max = 20) String plz,
            @NotBlank @Size(max = 120) String ort, @NotBlank @Pattern(regexp = "[A-Z]{2}") String countryCode,
            @Size(max = 500) String veranstalter, @Size(max = 3000) String bemerkung,
            @Min(0) Integer anzahlJugendliche, @Min(0) @Max(120) Integer jugendAlterVon,
            @Min(0) @Max(120) Integer jugendAlterBis, @Min(0) Integer anzahlErwachsene,
            @NotNull @AssertTrue Boolean hausregelnAkzeptiert, @NotNull @AssertTrue Boolean datenschutzGelesen) {}
    public record Bereich(Long id, String bezeichnung, String beschreibung, int bestand,
            String mengeneinheit, String berechnungsart, BigDecimal einzelpreis, List<Long> erforderlicheIds) {}
    public record Katalog(Long konfigurationVersion, String bezeichnung, String mietobjekt,
            String anreisezeit, String abreisezeit, String datenschutzUrl, String hausregelnUrl,
            boolean teilnehmerPflicht, List<Bereich> bereiche) {}
    public record KalenderAuswahl(@NotBlank @Pattern(regexp = "[0-9]{4}-(0[1-9]|1[0-2])") String monat,
            @NotNull @Positive Long mietbereichId) {}
    public record KalenderBelegung(java.time.LocalDateTime von, java.time.LocalDateTime bis, String status, String buchungsnummer, String mietername) {}
    public record KalenderTag(LocalDate datum, String status, int verfuegbar, List<String> buchungsnummern,
            List<KalenderBelegung> belegungen) {}
    public record Kalender(String monat, Long mietbereichId, List<KalenderTag> tage) {}
    public record Verfuegbarkeit(boolean verfuegbar, String hinweis) {}
    /** Nur für den authentifizierten Homepage-Server; Empfänger nicht öffentlich ausgeben. */
    public record Bestaetigung(Long buchungId, String buchungsnummer, String status,
            boolean gastBestaetigung, boolean verwaltungsBenachrichtigung, String empfaengerEmail, String mietumfang) {}
}
