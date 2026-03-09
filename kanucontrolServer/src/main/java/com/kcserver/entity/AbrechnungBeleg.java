package com.kcserver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "abrechnung_beleg")
@Getter
@Setter
public class AbrechnungBeleg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "abrechnung_id", nullable = false)
    private Abrechnung abrechnung;

    @OneToMany(mappedBy = "beleg",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<AbrechnungBuchung> positionen = new ArrayList<>();

    @Column(name = "beleg_nummer", nullable = false, length = 50)
    private String belegnummer;

    @Column(nullable = false)
    private LocalDate datum;

    @Column(length = 500)
    private String beschreibung;

    public void addPosition(AbrechnungBuchung position) {
        positionen.add(position);
        position.setBeleg(this);
    }

    public void removePosition(AbrechnungBuchung position) {
        positionen.remove(position);
        position.setBeleg(null);
    }
}