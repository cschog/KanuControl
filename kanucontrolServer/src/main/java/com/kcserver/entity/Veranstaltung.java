package com.kcserver.entity;

import com.kcserver.enumtype.CountryCode;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.persistence.converter.VeranstaltungTypConverter;
import jakarta.persistence.*;
import lombok.*;

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

    @Convert(converter = VeranstaltungTypConverter.class)
    @Column(nullable = false, length = 1)
    private VeranstaltungTyp typ;

    @Column(nullable = true, length = 200)
    private String artDerUnterkunft;

    @Column(nullable = true, length = 200)
    private String artDerVerpflegung;

    @Column(nullable = true)
    private String plz;

    @Column(nullable = true)
    private String ort;

    @Column(name = "laender_code", length = 2)
    private CountryCode laenderCode;

    @Column(nullable = false)
    private LocalDate beginnDatum;

    @Column(nullable = false)
    private LocalTime beginnZeit;

    @Column(nullable = false)
    private LocalDate endeDatum;

    @Column(nullable = false)
    private LocalTime endeZeit;

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


    @Column(nullable = true)
    private Integer geplanteTeilnehmerMaennlich;

    @Column(nullable = true)
    private Integer geplanteTeilnehmerWeiblich;

    @Column(nullable = true)
    private Integer geplanteTeilnehmerDivers;

    @Column(nullable = true)
    private Integer geplanteMitarbeiterMaennlich;

    @Column(nullable = true)
    private Integer geplanteMitarbeiterWeiblich;

    @Column(nullable = true)
    private Integer geplanteMitarbeiterDivers;

    /* =========================
       Status
       ========================= */

    @Column(nullable = false)
    private boolean aktiv;
}