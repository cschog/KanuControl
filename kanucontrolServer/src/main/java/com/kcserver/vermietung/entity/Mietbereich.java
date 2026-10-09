package com.kcserver.vermietung.entity;

import com.kcserver.core.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "mietbereich")
public class Mietbereich extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mietobjekt_id", nullable = false)
    private Mietobjekt mietobjekt;

    @Column(nullable = false)
    private String bezeichnung;

    @Column(length = 2000)
    private String beschreibung;

    @Column(nullable = false)
    private boolean mietbar = true;

    @Column(name = "direktbuchung_aktiv", nullable = false)
    private boolean direktbuchungAktiv = true;

    @Column(name = "airbnb_aktiv", nullable = false)
    private boolean airbnbAktiv = false;

    @Column(name = "bestand", nullable = false)
    private Integer bestand = 1;

    @Column(name = "mengeneinheit", length = 50)
    private String mengeneinheit;

    @OneToMany(
            mappedBy = "mietbereich",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MietbereichEigenschaft> eigenschaften = new ArrayList<>();
}