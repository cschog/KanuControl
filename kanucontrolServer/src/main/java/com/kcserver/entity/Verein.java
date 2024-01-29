package com.kcserver.entity;

import jakarta.persistence.*;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity(name = "verein")
@Getter
@ToString
@NoArgsConstructor(force = true)
@Table
public class Verein {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private final String name;

    @Column(name = "abk")
    private final String abk;

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

    @Column(name = "konto_inhaber")
    private final String kontoInhaber;

    @Column(name = "ki_anschrift")
    private final String kiAnschrift;

    @Column(name = "iban")
    private final String iban;

    @Column(name = "bic")
    private final String bic;

    @OneToMany(mappedBy = "vereinMitgliedschaft")
    private Set<Mitglied> mitglieder = new HashSet<>();

    // Parameterized constructor
    public Verein(String name, String abk, String strasse, String plz, String ort,
                  String telefon, String bankName, String kontoInhaber, String kiAnschrift,
                  String iban, String bic) {

        this.name = name;
        this.abk = abk;
        this.strasse = strasse;
        this.plz = plz;
        this.ort = ort;
        this.telefon = telefon;
        this.bankName = bankName;
        this.kontoInhaber = kontoInhaber;
        this.kiAnschrift = kiAnschrift;
        this.iban = iban;
        this.bic = bic;

    }

    public Set<Mitglied> getMitglieder() {
        return mitglieder;
    }

    public void setMitglieder(Set<Mitglied> mitglieder) {
        this.mitglieder = mitglieder;
    }

    public void addMitglied(Mitglied mitglied) {
        mitglieder.add(mitglied);
        mitglied.setVereinMitgliedschaft(this);
    }

    public void removeMitglied(Mitglied mitglied) {
        mitglieder.remove(mitglied);
        mitglied.setVereinMitgliedschaft(null);
    }

    public void setName(String name) {
    }

    public void setAbk(String abk) {
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

    public void setKontoInhaber(String kontoInhaber) {
    }

    public void setKiAnschrift(String kiAnschrift) {
    }

    public void setIban(String iban) {
    }

    public void setBic(String bic) {
    }
}
