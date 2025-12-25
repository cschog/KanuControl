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
@Table(name = "verein")
public class Verein extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    private String name;

    @NotNull
    @Size(min = 1, max = 10)
    private String abk;

    private String strasse;
    private String plz;
    private String ort;
    private String telefon;

    private String bankName;
    private String kontoInhaber;
    private String kiAnschrift;
    private String iban;
    private String bic;

    @OneToMany(
            mappedBy = "verein",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Mitglied> mitglieder = new ArrayList<>();

    // Convenience
    public void addMitglied(Mitglied mitglied) {
        mitglieder.add(mitglied);
        mitglied.setVerein(this);
    }

    public void removeMitglied(Mitglied mitglied) {
        mitglieder.remove(mitglied);
        mitglied.setVerein(null);
    }
}