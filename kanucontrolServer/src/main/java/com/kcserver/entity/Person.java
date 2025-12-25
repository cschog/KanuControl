package com.kcserver.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "person")
public class Person extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    private String name;

    @NotNull
    @Size(min = 2, max = 100)
    private String vorname;

    private String strasse;
    private String plz;
    private String ort;
    private String telefon;

    @Column(name = "bank_name")
    private String bankName;

    private String iban;
    private String bic;

    @OneToMany(
            mappedBy = "person",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Mitglied> mitgliedschaften = new ArrayList<>();

    // Convenience-Methoden
    public void addMitgliedschaft(Mitglied mitglied) {
        mitgliedschaften.add(mitglied);
        mitglied.setPerson(this);
    }

    public void removeMitgliedschaft(Mitglied mitglied) {
        mitgliedschaften.remove(mitglied);
        mitglied.setPerson(null);
    }
}