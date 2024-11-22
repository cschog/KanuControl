package com.kcserver.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity(name = "person")
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"iban", "telefon", "bic"}) // Exclude sensitive fields from string representation
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table
public class Person {

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

    // Audit Fields (optional)
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    // Pre-persist and Pre-update methods
    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        this.lastModifiedDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.lastModifiedDate = LocalDateTime.now();
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