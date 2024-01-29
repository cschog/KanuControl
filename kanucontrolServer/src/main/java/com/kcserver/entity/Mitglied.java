package com.kcserver.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@ToString
@NoArgsConstructor
@Table
public class Mitglied {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "verein_id")
    private Verein vereinMitgliedschaft;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person personMitgliedschaft;

    @Column(name = "funktion")
    private String funktion;

    @Column(name = "haupt_verein")
    private Boolean hauptVerein;

    // Other attributes...



    // Other methods...
    public void setVereinMitgliedschaft(Verein vereinMitgliedschaft) {
        this.vereinMitgliedschaft = vereinMitgliedschaft;
    }

    public void setPersonMitgliedschaft(Person personMitgliedschaft) {
        this.personMitgliedschaft = personMitgliedschaft;
    }


    public void setFunktion(String funktion) {
        this.funktion = funktion;
    }

    public void setHauptVerein(Boolean hauptVerein) {
        this.hauptVerein = hauptVerein;
    }
}

