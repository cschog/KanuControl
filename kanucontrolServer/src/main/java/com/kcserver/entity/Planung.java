package com.kcserver.entity;

import com.kcserver.enumtype.PlanungsStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import com.kcserver.audit.Auditable;

@Entity
@Table(name = "planung")
@Getter
@Setter
public class Planung extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(
            name = "veranstaltung_id",
            nullable = false,
            unique = true
    )
    private Veranstaltung veranstaltung;

    /* ==========================================
       Antragsdaten
       ========================================== */

    private Integer geplanteTeilnehmerMaennlich;
    private Integer geplanteTeilnehmerWeiblich;
    private Integer geplanteTeilnehmerDivers;

    private Integer geplanteMitarbeiterMaennlich;
    private Integer geplanteMitarbeiterWeiblich;
    private Integer geplanteMitarbeiterDivers;

    /* ==========================================
       Simulationsparameter
       ========================================== */

    private Integer teilnehmer;
    private Integer mitarbeiter;

    private boolean kikZertifiziert;

    @Column(precision = 10, scale = 2)
    private BigDecimal teilnehmerBeitragUnter21Jahre;

    @Column(precision = 10, scale = 2)
    private BigDecimal mitarbeiterBeitrag;

    @Column(precision = 10, scale = 2)
    private BigDecimal unterkunftPreisProPersonUndNacht;

    @Column(precision = 10, scale = 2)
    private BigDecimal verpflegungPreisProPersonUndTag;

    @Column(precision = 10, scale = 2)
    private BigDecimal honorare;

    @Column(precision = 10, scale = 2)
    private BigDecimal fahrtkosten;

    @Column(precision = 10, scale = 2)
    private BigDecimal verbrauchsmaterialProTag;

    @Column(precision = 10, scale = 2)
    private BigDecimal kultur;

    @Column(precision = 10, scale = 2)
    private BigDecimal miete;

    @Column(precision = 10, scale = 2)
    private BigDecimal sonstigeKostenProTag;

    @Column(precision = 10, scale = 2)
    private BigDecimal pfand;

    @Column(precision = 10, scale = 2)
    private BigDecimal sonstigeEinnahmenProTag;

    /* ==========================================
       Berechnetes Ergebnis
       ========================================== */

    @OneToMany(
            mappedBy = "planung",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PlanungPosition> positionen = new ArrayList<>();

    /* ==========================================
       Status
       ========================================== */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PlanungsStatus status = PlanungsStatus.IN_BEARBEITUNG;

    public void addPosition(PlanungPosition position) {
        positionen.add(position);
        position.setPlanung(this);
    }

    public void removePosition(PlanungPosition position) {
        positionen.remove(position);
        position.setPlanung(null);
    }

    public boolean istEingereicht() {
        return status == PlanungsStatus.EINGEREICHT;
    }

    public boolean istInBearbeitung() {
        return status == PlanungsStatus.IN_BEARBEITUNG;
    }
}
