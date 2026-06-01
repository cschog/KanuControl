package com.kcserver.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import com.kcserver.audit.Auditable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reisekostenabrechnung")
public class Reisekostenabrechnung extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "veranstaltung_id",
            nullable = false
    )
    private Veranstaltung veranstaltung;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "fahrer_id",
            nullable = false
    )
    private Person fahrer;

    @Column(name = "abrechnungsdatum")
    private LocalDate abrechnungsdatum;

    @Column(name = "gesamt_kilometer")
    private Integer gesamtKilometer;

    @Column(
            name = "gesamt_betrag",
            precision = 10,
            scale = 2
    )
    private BigDecimal gesamtBetrag;

    @Column(
            name = "bemerkung",
            length = 1000
    )
    private String bemerkung;

    @OneToMany(
            mappedBy = "abrechnung",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Fahrtabschnitt> fahrtabschnitte = new ArrayList<>();
}
