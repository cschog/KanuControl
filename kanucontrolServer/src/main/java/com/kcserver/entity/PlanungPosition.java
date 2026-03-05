package com.kcserver.entity;

import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.finanz.FinanzPosition;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "planung_position",
        uniqueConstraints = {
                // Optional: verhindert doppelte Kategorien pro Planung
                @UniqueConstraint(columnNames = {"planung_id", "kategorie"})
        }
)
@Getter
@Setter
public class PlanungPosition implements FinanzPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ================= Beziehung ================= */

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "planung_id", nullable = false)
    private Planung planung;

    /* ================= Finanzdaten ================= */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FinanzKategorie kategorie;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal betrag = BigDecimal.ZERO;

    @Column(length = 500)
    private String kommentar;

    /* ================= Interface ================= */

    @Override
    public FinanzKategorie getKategorie() {
        return kategorie;
    }

    @Override
    public BigDecimal getBetrag() {
        return betrag != null ? betrag : BigDecimal.ZERO;
    }
}