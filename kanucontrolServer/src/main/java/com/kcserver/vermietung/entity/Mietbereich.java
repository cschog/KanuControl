package com.kcserver.vermietung.entity;

import com.kcserver.core.audit.Auditable;
import com.kcserver.vermietung.enumtype.Berechnungsart;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Column(name = "sortierung", nullable = false)
    private Integer sortierung = 0;

    @Column(length = 2000)
    private String beschreibung;

    @Column(nullable = false)
    private boolean mietbar = true;

    @Column(name = "direktbuchung_aktiv", nullable = false)
    private boolean direktbuchungAktiv = true;

    @Column(name = "airbnb_aktiv", nullable = false)
    private boolean airbnbAktiv = false;

    @Column(name = "airbnb_inserat", nullable = false)
    private boolean airbnbInserat = false;

    @Column(name = "bestand", nullable = false)
    private Integer bestand = 1;

    @Column(name = "mengeneinheit", length = 50)
    private String mengeneinheit;

    @Enumerated(EnumType.STRING)
    @Column(name = "berechnungsart", nullable = false, length = 20)
    private Berechnungsart berechnungsart = Berechnungsart.TAG;

    @OneToMany(
            mappedBy = "mietbereich",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MietbereichRabatt> rabatte = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "mietbereich_zaehler",
            joinColumns = @JoinColumn(name = "mietbereich_id"),
            inverseJoinColumns = @JoinColumn(name = "zaehler_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_mietbereich_zaehler",
                    columnNames = {"mietbereich_id", "zaehler_id"}
            )
    )
    private Set<Zaehler> zaehler = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "mietbereich_voraussetzung",
            joinColumns = @JoinColumn(name = "mietbereich_id"),
            inverseJoinColumns = @JoinColumn(name = "erforderlicher_mietbereich_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_mietbereich_voraussetzung",
                    columnNames = {"mietbereich_id", "erforderlicher_mietbereich_id"}
            )
    )
    private Set<Mietbereich> voraussetzungenDirekt = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "mietbereich_voraussetzung_airbnb",
            joinColumns = @JoinColumn(name = "mietbereich_id"),
            inverseJoinColumns = @JoinColumn(name = "erforderlicher_mietbereich_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_mietbereich_voraussetzung_airbnb",
                    columnNames = {"mietbereich_id", "erforderlicher_mietbereich_id"}
            )
    )
    private Set<Mietbereich> voraussetzungenAirbnb = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "mietbereich_belegung",
            joinColumns = @JoinColumn(name = "mietbereich_id"),
            inverseJoinColumns = @JoinColumn(name = "belegter_mietbereich_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_mietbereich_belegung",
                    columnNames = {"mietbereich_id", "belegter_mietbereich_id"}
            )
    )
    private Set<Mietbereich> belegteMietbereiche = new HashSet<>();

    @OneToMany(
            mappedBy = "mietbereich",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MietbereichEigenschaft> eigenschaften = new ArrayList<>();
}
