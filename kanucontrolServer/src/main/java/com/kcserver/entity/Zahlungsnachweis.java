package com.kcserver.entity;

import jakarta.persistence.*;
import com.kcserver.audit.Auditable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "zahlungsnachweis")
public class Zahlungsnachweis extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veranstaltung_id", nullable = false)
    private Veranstaltung veranstaltung;

    private LocalDate datum;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal betrag;

    private String bemerkung;

    @OneToMany(
            mappedBy = "zahlungsnachweis",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("id ASC")
    private List<ZahlungsPosition> positionen = new ArrayList<>();



    @OneToMany(
            mappedBy = "zahlungsnachweis",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("reihenfolge ASC")
    private List<ZahlungsnachweisDokument> dokumente = new ArrayList<>();

    public void addPosition(ZahlungsPosition position) {
        positionen.add(position);
        position.setZahlungsnachweis(this);
    }

    public void removePosition(ZahlungsPosition position) {
        positionen.remove(position);
        position.setZahlungsnachweis(null);
    }

    public void addDokument(ZahlungsnachweisDokument dokument) {
        dokumente.add(dokument);
        dokument.setZahlungsnachweis(this);
    }

    public void removeDokument(ZahlungsnachweisDokument dokument) {
        dokumente.remove(dokument);
        dokument.setZahlungsnachweis(null);
    }

    public void clearPositionen() {
        for (ZahlungsPosition position : new ArrayList<>(positionen)) {
            removePosition(position);
        }
    }

    public void clearDokumente() {
        for (ZahlungsnachweisDokument dokument : new ArrayList<>(dokumente)) {
            removeDokument(dokument);
        }
    }

}


