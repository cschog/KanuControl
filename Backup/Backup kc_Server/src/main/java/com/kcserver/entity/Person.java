package com.kcserver.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "person")
@Entity
public class Person extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    @Column(name = "name")
    private String name;

    @NotNull
    @Size(min = 2, max = 100)
    @Column(name = "vorname")
    private String vorname;

    @Column(name = "strasse")
    private String strasse;

    @Column(name = "plz")
    private String plz;

    @Column(name = "ort")
    private String ort;

    @Column(name = "telefon")
    private String telefon;

    @Size(max = 255)
    @Column(name = "bank_name")
    private String bankName;

    // @Pattern(regexp = "^[A-Z0-9]{22}$") // Validate IBAN with a length of 22 (standard for many countries)
    @Column(name = "iban")
    private String iban;

    // @Pattern(regexp = "^[A-Z0-9]{8,11}$") // Validate BIC format with length between 8-11
    @Column(name = "bic")
    private String bic;

    @OneToMany(mappedBy = "personMitgliedschaft", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mitglied> mitgliedschaften = new ArrayList<>(); // Initialize with an empty list

    // Helper method to add a Mitglied
    public void addMitglied(Mitglied mitglied) {
        mitgliedschaften.add(mitglied);
        mitglied.setPerson(this);
    }

    // Helper method to remove a Mitglied
    public void removeMitglied(Mitglied mitglied) {
        mitgliedschaften.remove(mitglied);
        mitglied.setPerson(null);
    }


    public
    Person(String name, String vorname, String strasse, String plz, String ort,
           String telefon, String bankName, String iban, String bic) {
        this.name = name;
        this.vorname = vorname;
        this.strasse = strasse;
        this.plz = plz;
        this.ort = ort;
        this.telefon = telefon;
        this.bankName = bankName;
        this.iban = iban;
        this.bic = bic;
    }
}