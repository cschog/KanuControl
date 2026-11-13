package com.kcserver.core.entity;

import com.kcserver.kjfp.entity.Unterkunftsart;
import com.kcserver.kjfp.entity.Verpflegungsmodell;

import com.kcserver.core.audit.Auditable;
import com.kcserver.core.entity.Person;
import com.kcserver.core.entity.Verein;
import com.kcserver.kjfp.entity.beitraege.Beitragsstruktur;
import com.kcserver.kjfp.entity.beitraege.Zahlungsnachweis;
import com.kcserver.kjfp.enumtype.CountryCode;
import com.kcserver.core.enumtype.VeranstaltungScope;
import com.kcserver.kjfp.enumtype.VeranstaltungTyp;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "veranstaltung")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Veranstaltung extends Auditable {

    // Shared identity. Existing KJFP associations retain their mappings for API/DB compatibility.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* =========================
       Stammdaten
       ========================= */

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "programmentwurf_titel", length = 200)
    private String programmentwurfTitel;
    @Column(name = "programmentwurf_inhalt", columnDefinition = "text")
    private String programmentwurfInhalt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VeranstaltungTyp typ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unterkunftsart_id")
    private Unterkunftsart unterkunftsart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verpflegungsmodell_id")
    private Verpflegungsmodell verpflegungsmodell;


    @Column
    private String plz;

    @Column
    private String ort;

    @Column(name = "country_code", length = 2)
    private CountryCode countryCode;

    @Column(name = "beginn_datum", nullable = false)
    private LocalDate beginnDatum;

    @Column(name = "beginn_zeit")
    private LocalTime beginnZeit;

    @Column(name = "ende_datum", nullable = false)
    private LocalDate endeDatum;

    @Column(name = "ende_zeit")
    private LocalTime endeZeit;

    @Builder.Default
    @Column(name = "individuelle_gebuehren", nullable = false)
    private boolean individuelleGebuehren = false;

    @Column(name = "standard_gebuehr", precision = 10, scale = 2)
    private BigDecimal standardGebuehr;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VeranstaltungScope scope;

    /* =========================
       Beziehungen
       ========================= */

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "verein_id", nullable = false)
    private Verein verein;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leiter_id")
    private Person leiter;

    @Column(name = "beschreibung", length = 4000)
    private String beschreibung;

    @ManyToOne
    @JoinColumn(name = "beitragsstruktur_id")
    private Beitragsstruktur beitragsstruktur;

    @OneToMany(
            mappedBy = "veranstaltung",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Zahlungsnachweis> zahlungsnachweise;

    /* =========================
       Status
       ========================= */

    @Column(nullable = false)
    private boolean aktiv;

    @Column(name="kjfp_aktiv", nullable=false)
    private boolean kjfpAktiv = true;

    @Column(name="fahrten_aktiv", nullable=false)
    private boolean fahrtenAktiv = false;
}
