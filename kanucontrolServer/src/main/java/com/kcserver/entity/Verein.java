package com.kcserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    private String name;

    @Column(name = "abk")
    private  String abk;

    @Column(name = "strasse")
    private  String strasse;

    @Column(name = "plz")
    private  String plz;

    @Column(name = "ort")
    private  String ort;

    @Column(name = "telefon")
    private  String telefon;

    @Column(name = "bank_name")
    private  String bankName;

    @Column(name = "konto_inhaber")
    private  String kontoInhaber;

    @Column(name = "ki_anschrift")
    private  String kiAnschrift;

    @Column(name = "iban")
    private  String iban;

    @Column(name = "bic")
    private  String bic;

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

}
