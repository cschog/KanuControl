package com.kcserver.entity;

import com.kcserver.enumtype.BuchungsHerkunft;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.finanz.FinanzPosition;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "abrechnung_buchung")
@Getter
@Setter
public class AbrechnungBuchung implements FinanzPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "beleg_id", nullable = false)
    private AbrechnungBeleg beleg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teilnehmer_id")
    private Teilnehmer teilnehmer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reisekostenabrechnung_id")
    private Reisekostenabrechnung reisekostenabrechnung;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinanzKategorie kategorie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BuchungsHerkunft herkunft = BuchungsHerkunft.MANUELL;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal betrag;

    @Column(length = 500)
    private String beschreibung;

    @Override
    public FinanzKategorie getKategorie() {
        return kategorie;
    }

    @Override
    public BigDecimal getBetrag() {
        return betrag;
    }
}
