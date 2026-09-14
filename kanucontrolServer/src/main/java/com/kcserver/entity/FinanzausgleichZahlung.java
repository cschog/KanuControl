package com.kcserver.entity;

import com.kcserver.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "finanzausgleich_zahlung")
@Getter
@Setter
public class FinanzausgleichZahlung extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "finanz_gruppe_id", nullable = false)
    private FinanzGruppe finanzGruppe;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal betrag;

    @Column(nullable = false)
    private LocalDate datum;

    @Column(length = 500)
    private String bemerkung;

    @OneToMany(
            mappedBy = "finanzausgleichZahlung",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("reihenfolge ASC")
    private List<Dokument> dokumente = new ArrayList<>();

    public void addDokument(Dokument dokument) {
        dokumente.add(dokument);
        dokument.setFinanzausgleichZahlung(this);
    }

    public void removeDokument(Dokument dokument) {
        dokumente.remove(dokument);
        dokument.setFinanzausgleichZahlung(null);
    }
}