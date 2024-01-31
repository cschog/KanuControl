package com.kcserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    private  String name;

    @Column(name = "vorname")
    private  String vorname;

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

    @Column(name = "iban")
    private  String iban;

    @Column(name = "bic")
    private  String bic;


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

}
