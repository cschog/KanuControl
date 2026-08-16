package com.kcserver.entity;

import com.kcserver.audit.Auditable;
import com.kcserver.enumtype.BuchungsHerkunft;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "abrechnung_beleg",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"abrechnung_id", "beleg_nummer"}
        )
)
@Getter
@Setter
public class AbrechnungBeleg extends Auditable {

    /* =========================================================
       BASIS
       ========================================================= */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lfd_nr", nullable = false)
    private Integer lfdNr;

    /* =========================================================
       ZUORDNUNGEN
       ========================================================= */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "abrechnung_id", nullable = false)
    private Abrechnung abrechnung;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "finanz_gruppe_id", nullable = false)
    private FinanzGruppe finanzGruppe;

    /* =========================================================
       BELEGMETADATEN
       ========================================================= */

    @Column(name = "beleg_nummer", nullable = false, length = 50)
    private String belegnummer;

    @Column(name = "externe_beleg_nummer", length = 100)
    private String externeBelegnummer;

    @Column(length = 200)
    private String aussteller;

    @Column(nullable = false)
    private LocalDate datum;

    @Column(length = 500)
    private String beschreibung;

    /* =========================================================
       POSITIONEN
       ========================================================= */

    @OneToMany(
            mappedBy = "beleg",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("id ASC")
    private List<AbrechnungBuchung> positionen = new ArrayList<>();

  /* =========================================================
   DOKUMENTE
   ========================================================= */

    @OneToMany(
            mappedBy = "beleg",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("reihenfolge ASC")
    private List<Dokument> dokumente = new ArrayList<>();

    /* =========================================================
       POSITIONEN
       ========================================================= */

    public void addPosition(AbrechnungBuchung position) {
        positionen.add(position);
        position.setBeleg(this);
    }

    public void removePosition(AbrechnungBuchung position) {
        positionen.remove(position);
        position.setBeleg(null);
    }

    public void clearPositionen() {
        for (AbrechnungBuchung position : new ArrayList<>(positionen)) {
            removePosition(position);
        }
    }

    public void removePositionenByHerkunft(BuchungsHerkunft herkunft) {
        for (AbrechnungBuchung position : new ArrayList<>(positionen)) {
            if (position.getHerkunft() == herkunft) {
                removePosition(position);
            }
        }
    }

    /* =========================================================
       DOKUMENTE
       ========================================================= */

    public void addDokument(Dokument dokument) {
        dokumente.add(dokument);
        dokument.setBeleg(this);
    }

    public void removeDokument(Dokument dokument) {
        dokumente.remove(dokument);
        dokument.setBeleg(null);
    }

    public void clearDokumente() {
        for (Dokument dokument : new ArrayList<>(dokumente)) {
            removeDokument(dokument);
        }
    }
}