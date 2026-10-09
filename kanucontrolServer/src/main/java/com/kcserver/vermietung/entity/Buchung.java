package com.kcserver.vermietung.entity;

import com.kcserver.core.audit.Auditable;
import com.kcserver.kjfp.entity.Person;
import com.kcserver.kjfp.entity.Verein;
import com.kcserver.vermietung.enumtype.Buchungsquelle;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "buchung",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_buchung_jahr_laufende_nummer",
                        columnNames = {
                                "buchungsjahr",
                                "laufende_nummer"
                        }
                )
        }
)
public class Buchung extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(name = "buchungsjahr", nullable = false)
    private Integer buchungsjahr;

    @Column(name = "laufende_nummer", nullable = false)
    private Integer laufendeNummer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Buchungsstatus status = Buchungsstatus.ANFRAGE;

    @Enumerated(EnumType.STRING)
    @Column(name = "buchungsquelle", nullable = false, length = 20)
    private Buchungsquelle buchungsquelle;

    @Column(nullable = false)
    private LocalDate anreise;

    @Column(nullable = false)
    private LocalDate abreise;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mietobjekt_id", nullable = false)
    private Mietobjekt mietobjekt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mieter_id", nullable = false)
    private Person mieter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veranstalter_verein_id")
    private Verein veranstalter;

    @OneToMany(
            mappedBy = "buchung",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<BuchungMietbereich> mietbereiche = new ArrayList<>();

    @Transient
    public String getBuchungsnummer() {
        if (buchungsjahr == null || laufendeNummer == null) {
            return null;
        }

        return String.format(
                "%d-%03d",
                buchungsjahr,
                laufendeNummer
        );
    }
}