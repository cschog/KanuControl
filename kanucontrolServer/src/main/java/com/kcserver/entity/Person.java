package com.kcserver.entity;

import com.kcserver.audit.Auditable;
import com.kcserver.enumtype.CountryCode;
import com.kcserver.enumtype.Sex;
import com.kcserver.persistence.converter.CountryCodeConverter;
import com.kcserver.persistence.converter.SexConverter;
import com.kcserver.validation.OnCreate;
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

    @NotNull(groups = OnCreate.class)
    @Convert(converter = SexConverter.class)
    @Column(length = 1)
    private Sex sex;   // ✅ Enum M/W/D → CHAR(1)

    /* =========================
       Adresse & Kontakt
       ========================= */

    private String strasse;
    private String plz;
    private String ort;

    @Convert(converter = CountryCodeConverter.class)
    @Column(name = "country_code", length = 2)
    private CountryCode countryCode;

    @Column(name = "telefon_festnetz")
    private String telefonFestnetz;

    private String telefon;

    /* =========================
       Bankdaten
       ========================= */

    @Column(name = "bank_name")
    private String bankName;

    private String iban;

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