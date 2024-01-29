package com.kcserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity(name = "person")
@Getter
@ToString
@NoArgsConstructor(force = true)
@Table
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private final String name;

    @Column(name = "vorname")
    private final String vorname;

    @Column(name = "strasse")
    private final String strasse;

    @Column(name = "plz")
    private final String plz;

    @Column(name = "ort")
    private final String ort;

    @Column(name = "telefon")
    private final String telefon;

    @Column(name = "bank_name")
    private final String bankName;

    @Column(name = "iban")
    private final String iban;

    @Column(name = "bic")
    private final String bic;

    @OneToMany(mappedBy = "personMitgliedschaft")
    private final Set<Mitglied> mitglieder = new HashSet<>();


    // Parameterized constructor
    public Person(String name, String vorname, String strasse, String plz, String ort,
                  String telefon, String bankName,
                  String iban, String bic) {

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

    public void setName(String name) {
    }

    public void setVorname(String vorname) {
    }

    public void setStrasse(String strasse) {
    }

    public void setPlz(String plz) {
    }

    public void setOrt(String ort) {
    }

    public void setTelefon(String telefon) {
    }

    public void setBankName(String bankName) {
    }

    public void setIban(String iban) {
    }

    public void setBic(String bic) {
    }
}
