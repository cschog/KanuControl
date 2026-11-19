package com.kcserver.vermietung.entity;

import com.kcserver.core.audit.Auditable;
import com.kcserver.vermietung.enumtype.Verbrauchsabrechnungsart;
import com.kcserver.vermietung.enumtype.Verbrauchsart;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "verbrauchskosten")
public class Verbrauchskosten extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mietobjekt_id", nullable = false)
    private Mietobjekt mietobjekt;

    @Column(nullable = false, length = 100)
    private String bezeichnung;

    @Enumerated(EnumType.STRING)
    @Column(name = "verbrauchsart", nullable = false, length = 20)
    private Verbrauchsart verbrauchsart;

    @Enumerated(EnumType.STRING)
    @Column(name = "abrechnungsart", nullable = false, length = 20)
    private Verbrauchsabrechnungsart abrechnungsart;

    @Column(name = "mengeneinheit", length = 20)
    private String mengeneinheit;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preis;

    @ElementCollection
    @CollectionTable(name = "verbrauchskosten_preis", joinColumns = @JoinColumn(name = "verbrauchskosten_id"))
    @OrderBy("gueltigVon ASC")
    private java.util.List<Verbrauchspreis> preise = new java.util.ArrayList<>();

    /** Liefert eine unverwaltete Konfiguration mit dem Preis am Anreisetag. */
    public Verbrauchskosten zumDatum(java.time.LocalDate datum) {
        Verbrauchspreis tarif = preise.stream().filter(p -> !datum.isBefore(p.getGueltigVon())
                && (p.getGueltigBis() == null || !datum.isAfter(p.getGueltigBis())))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "Kein gültiger Preis für „" + bezeichnung + "“ am " + datum + "."));
        Verbrauchskosten kopie = new Verbrauchskosten();
        kopie.setId(id); kopie.setMietobjekt(mietobjekt); kopie.setBezeichnung(bezeichnung);
        kopie.setVerbrauchsart(verbrauchsart); kopie.setAbrechnungsart(abrechnungsart);
        kopie.setMengeneinheit(mengeneinheit); kopie.setAktiv(aktiv); kopie.setPreis(tarif.getPreis());
        return kopie;
    }

    /** Kompatibilitätsfeld für ältere Clients; die Preiswahl erfolgt ausschließlich nach Datum. */
    @Column(nullable = false)
    private boolean aktiv = true;
}