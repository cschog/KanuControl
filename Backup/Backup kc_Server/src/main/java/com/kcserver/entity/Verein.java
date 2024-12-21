package com.kcserver.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Entity(name = "verein")
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"iban", "bic"}) // Optionally exclude sensitive fields from string representation
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table
public class Verein extends Auditable {

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
    @Size(min = 1, max = 10)
    @Column(name = "abk")
    private String abk;

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

    @Size(max = 255)
    @Column(name = "konto_inhaber")
    private String kontoInhaber;

    @Size(max = 255)
    @Column(name = "ki_anschrift")
    private String kiAnschrift;

    // @Pattern(regexp = "^[A-Z0-9]+$", message = "Invalid IBAN format")
    @Column(name = "iban")
    private String iban;

    // @Pattern(regexp = "^[A-Z0-9]+$", message = "Invalid BIC format")
    @Column(name = "bic")
    private String bic;


    public
    Verein(String name, String abk, String strasse, String plz, String ort,
           String telefon, String bankName, String kontoInhaber,
           String kiAnschrift, String iban, String bic) {
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