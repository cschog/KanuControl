package com.kcserver.entity.vermietung;

import com.kcserver.audit.Auditable;
import com.kcserver.entity.Verein;
import com.kcserver.enumtype.CountryCode;
import com.kcserver.persistence.converter.CountryCodeConverter;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "verein_id", nullable = false)
    private Verein verein;

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

    @OneToMany(
            mappedBy = "mietobjekt",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Mietbereich> mietbereiche = new ArrayList<>();
}