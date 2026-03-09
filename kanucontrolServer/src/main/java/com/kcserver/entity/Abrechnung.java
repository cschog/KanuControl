package com.kcserver.entity;

import com.kcserver.enumtype.AbrechnungsStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "abrechnung")
@Getter
@Setter
public class Abrechnung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* =========================================================
       VERANSTALTUNG
       ========================================================= */

    @OneToOne(optional = false)
    @JoinColumn(
            name = "veranstaltung_id",
            nullable = false,
            unique = true
    )
    private Veranstaltung veranstaltung;

    /* =========================================================
       BELEGE
       ========================================================= */

    @OneToMany(
            mappedBy = "abrechnung",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<AbrechnungBeleg> belege = new ArrayList<>();

    /* =========================================================
       STATUS
       ========================================================= */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AbrechnungsStatus status = AbrechnungsStatus.OFFEN;

    /* =========================================================
       SNAPSHOT FÖRDERSATZ (REVISIONS-SICHER)
       ========================================================= */

    /**
     * Der beim Abschließen verwendete Fördersatz pro Teilnehmer.
     * Wird gesetzt, sobald die Abrechnung abgeschlossen wird.
     * Danach unveränderlich.
     */
    @Column(
            name = "verwendeter_foerdersatz",
            precision = 10,
            scale = 2
    )
    private BigDecimal verwendeterFoerdersatz;

    /* =========================================================
       HELPER
       ========================================================= */

    public void addBeleg(AbrechnungBeleg beleg) {
        belege.add(beleg);
        beleg.setAbrechnung(this);
    }

    public void removeBeleg(AbrechnungBeleg beleg) {
        belege.remove(beleg);
        beleg.setAbrechnung(null);
    }

    /**
     * Domain-Regel:
     * Fördersatz darf nur gesetzt werden,
     * solange Abrechnung noch OFFEN ist.
     */
    public void setVerwendeterFoerdersatzIfOpen(BigDecimal satz) {
        if (this.status != AbrechnungsStatus.OFFEN) {
            throw new IllegalStateException(
                    "Fördersatz kann nur bei offener Abrechnung gesetzt werden"
            );
        }
        this.verwendeterFoerdersatz = satz;
    }
}