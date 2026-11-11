package com.kcserver.core.entity;

import com.kcserver.core.audit.Auditable;
import com.kcserver.kjfp.enumtype.CountryCode;
import com.kcserver.kjfp.enumtype.Sex;
import com.kcserver.core.converter.CountryCodeConverter;
import com.kcserver.core.converter.SexConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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
@Table(name = "person")
public class Person extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    /* =========================
       Stammdaten
       ========================= */

    @NotNull
    @Size(min = 2, max = 100)
    private String name;

    @NotNull
    @Size(min = 2, max = 100)
    private String vorname;

    private LocalDate geburtsdatum;

    @Convert(converter = SexConverter.class)
    @Column(length = 1)
    private Sex sex;   // ✅ Enum M/W/D → CHAR(1)

    /** Personen aus dem Vermietungsbereich bleiben aus KJFP-Listen ausgeschlossen. */
    @Column(name = "kjfp_sichtbar", nullable = false)
    private boolean kjfpSichtbar = true;

    @Email
    @Column(length = 255)
    private String email;

    /* =========================
       Adresse & Kontakt
       ========================= */

    private String strasse;
    private String plz;
    private String ort;

    @NotNull
    @Convert(converter = CountryCodeConverter.class)
    @Column(name = "country_code", nullable = false, length = 2)
    private CountryCode countryCode = CountryCode.DE;

    @Column(name = "telefon_festnetz")
    private String telefonFestnetz;

    private String telefon;

    /* =========================
       Bankdaten
       ========================= */

    @Column(length=255)
    private String organisation;

    @Column(length=255)
    private String kontoinhaber;

    @Column(name = "bank_name")
    private String bankName;

    private String iban;

    @Column(length = 20)
    private String bic;

    private LocalDate efz;

    /* aktiv = true
    „Diese Person soll standardmäßig bei der
    Auswahl von Teilnehmern sichtbar sein“
     */

    @Column(nullable = false)
    private boolean aktiv = true;

    /* =========================
       Beziehungen
       ========================= */

    @OneToMany(
            mappedBy = "person",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Mitglied> mitgliedschaften = new ArrayList<>();

    /* =========================
       Convenience
       ========================= */

    public void addMitgliedschaft(Mitglied mitglied) {
        mitgliedschaften.add(mitglied);
        mitglied.setPerson(this);
    }

    public void removeMitgliedschaft(Mitglied mitglied) {
        mitgliedschaften.remove(mitglied);
        mitglied.setPerson(null);
    }
}
