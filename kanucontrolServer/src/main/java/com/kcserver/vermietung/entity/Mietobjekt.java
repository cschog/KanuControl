package com.kcserver.vermietung.entity;

import com.kcserver.core.audit.Auditable;
import com.kcserver.kjfp.enumtype.CountryCode;
import com.kcserver.core.converter.CountryCodeConverter;
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

    @Column(name = "sortierung", nullable = false)
    private Integer sortierung = 0;

    @Column(length = 2000)
    private String beschreibung;

    private String strasse;

    private String plz;

    private String ort;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "kontoinhaber", column = @Column(name = "bank_primaer_kontoinhaber", length = 255)),
        @AttributeOverride(name = "bankName", column = @Column(name = "bank_primaer_name", length = 255)),
        @AttributeOverride(name = "iban", column = @Column(name = "bank_primaer_iban", length = 34)),
        @AttributeOverride(name = "bic", column = @Column(name = "bank_primaer_bic", length = 11))
    })
    private Bankverbindung bankverbindungPrimaer;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "kontoinhaber", column = @Column(name = "bank_alternativ_kontoinhaber", length = 255)),
        @AttributeOverride(name = "bankName", column = @Column(name = "bank_alternativ_name", length = 255)),
        @AttributeOverride(name = "iban", column = @Column(name = "bank_alternativ_iban", length = 34)),
        @AttributeOverride(name = "bic", column = @Column(name = "bank_alternativ_bic", length = 11))
    })
    private Bankverbindung bankverbindungAlternativ;


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

    @OneToMany(mappedBy = "mietobjekt", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("von ASC, id ASC")
    private List<MietobjektSperrzeit> sperrzeiten = new ArrayList<>();
}