package com.kcserver.entity;

import com.kcserver.audit.Auditable;
import com.kcserver.enumtype.CountryCode;
import com.kcserver.enumtype.VeranstaltungTyp;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "veranstaltung")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Veranstaltung extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* =========================
       Stammdaten
       ========================= */

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private VeranstaltungTyp typ;

    @Column(name = "art_der_unterkunft", length = 200)
    private String artDerUnterkunft;

    @Column(name = "art_der_verpflegung", length = 200)
    private String artDerVerpflegung;

    @Column(nullable = true)
    private String plz;

    @Column(nullable = true)
    private String ort;

    @Column(name = "country_code", length = 2)
    private CountryCode laenderCode;

    @Column(name = "beginn_datum", nullable = false)
    private LocalDate beginnDatum;

    @Column(name = "beginn_zeit", nullable = false)
    private LocalTime beginnZeit;

    @Column(name = "ende_datum", nullable = false)
    private LocalDate endeDatum;

    @Column(name = "ende_zeit", nullable = false)
    private LocalTime endeZeit;

    @Builder.Default
    @Column(name = "individuelle_gebuehren", nullable = false)
    private boolean individuelleGebuehren = false;

    @Column(name = "standard_gebuehr", precision = 10, scale = 2)
    private BigDecimal standardGebuehr;

    /* =========================
       Beziehungen
       ========================= */

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "verein_id", nullable = false)
    private Verein verein;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "leiter_id", nullable = false)
    private Person leiter;

    /* =========================
       Plan-Zahlen (Antrag)
       ========================= */

    @Column(name = "geplante_teilnehmer_maennlich")
    private Integer geplanteTeilnehmerMaennlich;

    @Column(name = "geplante_teilnehmer_weiblich")
    private Integer geplanteTeilnehmerWeiblich;

    @Column(name = "geplante_teilnehmer_divers")
    private Integer geplanteTeilnehmerDivers;

    @Column(name = "geplante_mitarbeiter_maennlich")
    private Integer geplanteMitarbeiterMaennlich;

    @Column(name = "geplante_mitarbeiter_weiblich")
    private Integer geplanteMitarbeiterWeiblich;

    @Column(name = "geplante_mitarbeiter_divers")
    private Integer geplanteMitarbeiterDivers;

    /* =========================
       Status
       ========================= */

    @Column(nullable = false)
    private boolean aktiv;
}