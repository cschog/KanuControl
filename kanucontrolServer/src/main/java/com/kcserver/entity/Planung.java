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

    @Column(name = "geplante_mitarbeiter_divers")
    private Integer geplanteMitarbeiterDivers;

    @Column(name = "geplante_mitarbeiter_maennlich")
    private Integer geplanteMitarbeiterMaennlich;

    @Column(name = "geplante_mitarbeiter_weiblich")
    private Integer geplanteMitarbeiterWeiblich;

    @Column(name = "geplante_teilnehmer_divers")
    private Integer geplanteTeilnehmerDivers;

    @Column(name = "geplante_teilnehmer_maennlich")
    private Integer geplanteTeilnehmerMaennlich;

    @Column(name = "geplante_teilnehmer_weiblich")
    private Integer geplanteTeilnehmerWeiblich;

    /* ==========================================
       Simulationsparameter
       ========================================== */

    private Integer teilnehmer;
    private Integer mitarbeiter;

    @Column(name = "kik_zertifiziert")
    private boolean kikZertifiziert;

    @Column(precision = 10, scale = 2, name = "teilnehmer_beitrag_unter21_jahre")
    private BigDecimal teilnehmerBeitragUnter21Jahre;

    @Column(name = "mitarbeiter_beitrag", precision = 10, scale = 2)
    private BigDecimal mitarbeiterBeitrag;

    @Column(precision = 10, scale = 2, name = "unterkunft_preis_pro_person_und_nacht")
    private BigDecimal unterkunftPreisProPersonUndNacht;

    @Column(precision = 10, scale = 2, name = "verpflegung_preis_pro_person_und_tag")
    private BigDecimal verpflegungPreisProPersonUndTag;

    @Column(precision = 10, scale = 2)
    private BigDecimal honorare;

    @Column(precision = 10, scale = 2)
    private BigDecimal fahrtkosten;

    @Column(precision = 10, scale = 2, name = "verbrauchsmaterial_pro_tag")
    private BigDecimal verbrauchsmaterialProTag;

    @Column(precision = 10, scale = 2)
    private BigDecimal kultur;

    @Column(precision = 10, scale = 2)
    private BigDecimal miete;

    @Column(precision = 10, scale = 2, name = "sonstige_kosten_pro_tag")
    private BigDecimal sonstigeKostenProTag;


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
