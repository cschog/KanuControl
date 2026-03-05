package com.kcserver.entity;

import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.finanz.FinanzPosition;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import java.time.LocalDate;

@Entity
@Table(name = "abrechnung_buchung")
@Getter
@Setter
public class AbrechnungBuchung implements FinanzPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ================= Beziehung ================= */

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "abrechnung_id", nullable = false)
    private Abrechnung abrechnung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private Person person; // optional, aber wenn gesetzt → Kürzel muss existieren

    /* ================= Fachliche Daten ================= */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinanzKategorie kategorie;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal betrag;

    @Column(nullable = false)
    private LocalDate datum;

    @Column(length = 500)
    private String beschreibung;

    /* ================= Interface ================= */

    @Override
    public FinanzKategorie getKategorie() {
        return kategorie;
    }

    @Override
    public BigDecimal getBetrag() {
        return betrag != null ? betrag : BigDecimal.ZERO;
    }

    /* ================= Derived ================= */

    public String getKuerzel() {
        return person != null ? person.getKuerzel() : null;
    }
}
