package com.kcserver.entity;

import com.kcserver.enumtype.AbrechnungsStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @OneToOne(optional = false)
    @JoinColumn(
            name = "veranstaltung_id",
            nullable = false,
            unique = true
    )
    private Veranstaltung veranstaltung;

    @OneToMany(mappedBy = "abrechnung",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<AbrechnungBuchung> buchungen = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AbrechnungsStatus status = AbrechnungsStatus.OFFEN;

    public void addBuchung(AbrechnungBuchung b) {
        buchungen.add(b);
        b.setAbrechnung(this);
    }

    public void removeBuchung(AbrechnungBuchung b) {
        buchungen.remove(b);
        b.setAbrechnung(null);
    }
}