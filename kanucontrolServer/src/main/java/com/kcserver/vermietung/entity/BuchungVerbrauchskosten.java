package com.kcserver.vermietung.entity;

import com.kcserver.core.audit.Auditable;
import com.kcserver.vermietung.enumtype.Verbrauchsabrechnungsart;
import com.kcserver.vermietung.enumtype.Verbrauchsart;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "buchung_verbrauchskosten")
public class BuchungVerbrauchskosten extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buchung_id", nullable = false)
    private Buchung buchung;

    /**
     * ID der ursprünglichen Verbrauchskostenkonfiguration.
     * Bewusst keine Entity-Beziehung, damit der Snapshot
     * unabhängig von der Konfiguration erhalten bleibt.
     */
    @Column(name = "verbrauchskosten_id", nullable = false)
    private Long verbrauchskostenId;

    @Column(nullable = false, length = 100)
    private String bezeichnung;

    @Enumerated(EnumType.STRING)
    @Column(name = "verbrauchsart", nullable = false, length = 20)
    private Verbrauchsart verbrauchsart;

    @Enumerated(EnumType.STRING)
    @Column(name = "abrechnungsart", nullable = false, length = 20)
    private Verbrauchsabrechnungsart abrechnungsart;

    @Column(name = "mengeneinheit", length = 20)
    private String mengeneinheit;

    /**
     * Der bei der Abrechnung verwendete Preis.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preis;

    /**
     * Tatsächlich verwendeter Anfangsstand.
     * Bei PAUSCHALE und TAGESPREIS null.
     */
    @Column(name = "anfangsstand", precision = 15, scale = 3)
    private BigDecimal anfangsstand;

    /**
     * Tatsächlich verwendeter Endstand.
     * Bei PAUSCHALE und TAGESPREIS null.
     */
    @Column(name = "endstand", precision = 15, scale = 3)
    private BigDecimal endstand;

    /**
     * Differenz zwischen End- und Anfangsstand.
     */
    @Column(precision = 15, scale = 3)
    private BigDecimal verbrauch;

    /**
     * Zeitpunkt der verwendeten Anfangsablesung.
     */
    @Column(name = "anfangsablesung")
    private LocalDateTime anfangsablesung;

    /**
     * Zeitpunkt der verwendeten Endablesung.
     */
    @Column(name = "endablesung")
    private LocalDateTime endablesung;

    /**
     * Anzahl der abgerechneten Tage bei TAGESPREIS.
     */
    @Column(name = "abrechnungstage")
    private Integer abrechnungstage;

    /**
     * Endgültiger Betrag dieser Verbrauchskostenposition.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal kosten;

    /**
     * ID des abgerechneten Zählers zum Zeitpunkt der Abrechnung.
     * Bei Pauschalen und Tagespreisen null.
     */
    @Column(name = "zaehler_id")
    private Long zaehlerId;

    /**
     * Bezeichnung des Zählers zum Zeitpunkt der Abrechnung.
     * Bleibt auch bei einer späteren Umbenennung erhalten.
     */
    @Column(name = "zaehler_bezeichnung", length = 100)
    private String zaehlerBezeichnung;
}