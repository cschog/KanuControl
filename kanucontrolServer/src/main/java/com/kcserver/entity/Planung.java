package com.kcserver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "planung")
@Getter
@Setter
public class Planung {

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

    @OneToMany(mappedBy = "planung",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PlanungPosition> positionen = new ArrayList<>();

    private boolean eingereicht;

    public void addPosition(PlanungPosition position) {
        positionen.add(position);
        position.setPlanung(this);
    }

    public void removePosition(PlanungPosition position) {
        positionen.remove(position);
        position.setPlanung(null);
    }
}
