package com.kcserver.entity;

import com.kcserver.audit.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "verein")
public class Verein extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    private String name;

    @NotNull
    @Size(min = 1, max = 10)
    private String abk;

    private String strasse;
    private String plz;
    private String ort;
    private String telefon;

    @Column(name = "bank_name")
    private String bankName;

    private String iban;

    @Column(length = 20)
    private String bic;

    private LocalDate schutzkonzept;

    // KiK-Zertifikat
    @Column(name = "kik_zertifiziert_seit")
    private LocalDate kikZertifiziertSeit;

    @Column(name = "kik_zertifiziert_bis")
    private LocalDate kikZertifiziertBis;

    public boolean isKikZertifiziertAm(LocalDate datum) {
        if (kikZertifiziertSeit == null) return false;

        boolean startOk = !datum.isBefore(kikZertifiziertSeit);
        boolean endOk = kikZertifiziertBis == null || !datum.isAfter(kikZertifiziertBis);

        return startOk && endOk;
    }

    /**
     * 🔗 NEU: Kontoinhaber als Person
     */
    @OneToOne(optional = true)
    @JoinColumn(name = "kontoinhaber_id", nullable = true)
    private Person kontoinhaber;

    @OneToMany(
            mappedBy = "verein",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Mitglied> mitglieder = new ArrayList<>();

    // Convenience
    public void addMitglied(Mitglied mitglied) {
        mitglieder.add(mitglied);
        mitglied.setVerein(this);
    }

    public void removeMitglied(Mitglied mitglied) {
        mitglieder.remove(mitglied);
        mitglied.setVerein(null);
    }
}