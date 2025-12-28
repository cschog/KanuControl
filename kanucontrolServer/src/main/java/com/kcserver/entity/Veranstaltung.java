package com.kcserver.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
    @Column(nullable = false)
    private VeranstaltungTyp typ;

    @Column(nullable = false)
    private LocalDate beginn;

    @Column(nullable = false)
    private LocalDate ende;

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

    @Column(nullable = false)
    private Integer geplanteTeilnehmer;

    @Column(nullable = false)
    private Integer geplanteTeilnehmerMaennlich;

    @Column(nullable = false)
    private Integer geplanteTeilnehmerWeiblich;

    @Column(nullable = false)
    private Integer geplanteMitarbeiter;

    /* =========================
       Status
       ========================= */

    @Column(nullable = false)
    private boolean aktiv;
}