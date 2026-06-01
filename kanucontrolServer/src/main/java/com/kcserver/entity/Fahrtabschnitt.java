package com.kcserver.entity;

import com.kcserver.enumtype.CountryCode;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fahrtabschnitt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Fahrtabschnitt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "abrechnung_id",
            nullable = false
    )
    private Reisekostenabrechnung abrechnung;

    @Column(name = "reihenfolge")
    private Integer reihenfolge;

    @Column(name = "von_plz", length = 20)
    private String vonPlz;

    @Column(name = "von_ort", nullable = false, length = 200)
    private String vonOrt;

    @Enumerated(EnumType.STRING)
    @Column(name = "von_country_code", length = 2)
    private CountryCode vonCountryCode;

    @Column(name = "nach_plz", length = 20)
    private String nachPlz;

    @Column(name = "nach_ort", nullable = false, length = 200)
    private String nachOrt;

    @Enumerated(EnumType.STRING)
    @Column(name = "nach_country_code", length = 2)
    private CountryCode nachCountryCode;

    @Column(
            name = "beschreibung",
            length = 200
    )
    private String beschreibung;

    @Column(
            name = "kilometer",
            nullable = false
    )
    private Integer kilometer;

    @Column(
            name = "anhaenger",
            nullable = false
    )
    private boolean anhaenger = false;

    @OneToMany(
            mappedBy = "fahrtabschnitt",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<FahrtabschnittMitfahrer> mitfahrer =
            new ArrayList<>();
}
