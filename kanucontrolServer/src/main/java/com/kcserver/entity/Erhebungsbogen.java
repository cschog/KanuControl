package com.kcserver.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "erhebungsbogen",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_erhebungsbogen_veranstaltung",
                columnNames = "veranstaltung_id"
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Erhebungsbogen extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* =========================
       Bezug
       ========================= */

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "veranstaltung_id", nullable = false)
    private Veranstaltung veranstaltung;

    /* =========================
       Status
       ========================= */

    @Column(nullable = false)
    private boolean abgeschlossen;

    @Column(nullable = false)
    private LocalDate stichtag; // i.d.R. Beginn-Datum der Veranstaltung

    /* =========================
       Statistik – Teilnehmer
       ========================= */

    private Integer teilnehmerMaennlichU6;
    private Integer teilnehmerMaennlich6_13;
    private Integer teilnehmerMaennlich14_17;
    private Integer teilnehmerMaennlich18Plus;

    private Integer teilnehmerWeiblichU6;
    private Integer teilnehmerWeiblich6_13;
    private Integer teilnehmerWeiblich14_17;
    private Integer teilnehmerWeiblich18Plus;

    private Integer teilnehmerDiversU6;
    private Integer teilnehmerDivers6_13;
    private Integer teilnehmerDivers14_17;
    private Integer teilnehmerDivers18Plus;

    /* =========================
       Statistik – Mitarbeitende
       ========================= */

    private Integer mitarbeiterMaennlich;
    private Integer mitarbeiterWeiblich;
    private Integer mitarbeiterDivers;

    /* =========================
       Optional
       ========================= */

    @Column(length = 500)
    private String bemerkung;
}