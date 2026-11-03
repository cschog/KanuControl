package com.kcserver.vermietung.entity;

import com.kcserver.core.audit.Auditable;
import com.kcserver.core.entity.Person;
import com.kcserver.core.entity.Verein;
import com.kcserver.vermietung.enumtype.Buchungsquelle;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.enumtype.Rabattart;
import com.kcserver.vermietung.enumtype.RabattwertTyp;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "buchung",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_buchung_jahr_laufende_nummer",
                        columnNames = {
                                "buchungsjahr",
                                "laufende_nummer"
                        }
                )
        }
)
public class Buchung extends Auditable {

    private static final LocalDateTime UNBEFRISTET_BIS =
            LocalDateTime.of(3000, 12, 31, 14, 0);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(name = "buchungsjahr", nullable = false)
    private Integer buchungsjahr;

    @Column(name = "laufende_nummer", nullable = false)
    private Integer laufendeNummer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Buchungsstatus status = Buchungsstatus.ANFRAGE;

    @Enumerated(EnumType.STRING)
    @Column(name = "buchungsquelle", nullable = false, length = 20)
    private Buchungsquelle buchungsquelle;

    @Enumerated(EnumType.STRING)
    @Column(name = "rabattart", nullable = false, length = 20)
    private Rabattart rabattart = Rabattart.KEIN_RABATT;

    @Enumerated(EnumType.STRING)
    @Column(name = "rabattwert_typ", length = 20)
    private RabattwertTyp rabattwertTyp;

    @Column(name = "rabattwert", precision = 10, scale = 2)
    private BigDecimal rabattwert;

    @Column(name = "rabattbetrag", nullable = false, precision = 10, scale = 2)
    private BigDecimal rabattbetrag = BigDecimal.ZERO;

    @Column(name = "kaution_betrag", nullable = false, precision = 10, scale = 2)
    private BigDecimal kautionBetrag = BigDecimal.ZERO;

    @Column(name = "anzahlung_betrag", nullable = false, precision = 10, scale = 2)
    private BigDecimal anzahlungBetrag = BigDecimal.ZERO;

    @Column(name = "anreise", nullable = false)
    private LocalDateTime anreise;

    @Column(name = "abreise", nullable = false)
    private LocalDateTime abreise;

    @Column(name = "anzahl_jugendliche")
    private Integer anzahlJugendliche;

    @Column(name = "jugend_alter_von")
    private Integer jugendAlterVon;

    @Column(name = "jugend_alter_bis")
    private Integer jugendAlterBis;

    @Column(name = "anzahl_erwachsene")
    private Integer anzahlErwachsene;

    @Column(name = "herkunft", length = 40)
    private String herkunft;

    @Column(columnDefinition = "TEXT")
    private String bemerkung;

    /**
     * Optionale Bewertung der Buchung, beispielsweise 1 bis 5 Sterne.
     */
    @Column
    private Integer bewertung;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mietobjekt_id", nullable = false)
    private Mietobjekt mietobjekt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mieter_id", nullable = false)
    private Person mieter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veranstalter_verein_id")
    private Verein veranstalter;

    @OneToMany(
            mappedBy = "buchung",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<BuchungMietbereich> mietbereiche = new ArrayList<>();

    @Transient
    public String getBuchungsnummer() {
        if (buchungsjahr == null || laufendeNummer == null) {
            return null;
        }

        return String.format(
                "%d-%03d",
                buchungsjahr,
                laufendeNummer
        );
    }

    @Transient
    public boolean isUnbefristet() {
        return UNBEFRISTET_BIS.equals(abreise);
    }
}
