package com.kcserver.vermietung.entity;

import com.kcserver.core.audit.Auditable;
import com.kcserver.kjfp.enumtype.CountryCode;
import com.kcserver.core.converter.CountryCodeConverter;
import com.kcserver.vermietung.enumtype.Berechnungsart;
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
@Table(name = "mietobjekt")
public class Mietobjekt extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(nullable = false)
    private String bezeichnung;

    @Column(length = 2000)
    private String beschreibung;

    private String strasse;

    private String plz;

    private String ort;

    @Convert(converter = CountryCodeConverter.class)
    @Column(name = "country_code", nullable = false, length = 2)
    private CountryCode countryCode = CountryCode.DE;

    @Column(nullable = false)
    private boolean aktiv = true;

    @Column(nullable = false)
    private boolean mietbar = true;

    @Column(name = "direktbuchung_aktiv", nullable = false)
    private boolean direktbuchungAktiv = true;

    @Column(name = "airbnb_aktiv", nullable = false)
    private boolean airbnbAktiv = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "berechnungsart", nullable = false, length = 20)
    private Berechnungsart berechnungsart = Berechnungsart.ZEITRAUM;

    @Column(name = "anreisezeit")
    private java.time.LocalTime anreisezeit;

    @Column(name = "abreisezeit")
    private java.time.LocalTime abreisezeit;

    @OneToMany(
            mappedBy = "mietobjekt",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Mietbereich> mietbereiche = new ArrayList<>();
}