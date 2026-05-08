package com.kcserver.entity;

import jakarta.persistence.*;
import com.kcserver.audit.Auditable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Beitragsstruktur extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private boolean aktiv;

    @Column(name = "is_template", nullable = false)
    private boolean template;

    @Column(name = "system", nullable = false)
    private boolean system;

    @OneToMany(
            mappedBy = "struktur",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Beitragsregel> regeln;
}
