package com.kcserver.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "mitglied", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"person_id", "verein_id"})
})
public class Mitglied extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "verein_id", nullable = false)
    @ToString.Exclude
    private Verein vereinMitgliedschaft;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    @ToString.Exclude
    private Person personMitgliedschaft;

    @Column(name = "funktion")
    private String funktion;

    @NotNull
    @Column(name = "haupt_verein")
    private Boolean hauptVerein;

    // Constructor with specific fields
    public Mitglied(Verein vereinMitgliedschaft, Person personMitgliedschaft, String funktion, Boolean hauptVerein) {
        this.vereinMitgliedschaft = vereinMitgliedschaft;
        this.personMitgliedschaft = personMitgliedschaft;
        this.funktion = funktion;
        this.hauptVerein = hauptVerein;
    }

    public boolean isHauptVerein() {
        return hauptVerein;
    }

    public void setHauptVerein(boolean hauptVerein) {
        this.hauptVerein = hauptVerein;
    }

    // Custom setter for Verein
    public void setVerein(Verein verein) {
        this.vereinMitgliedschaft = verein;
    }

    // Custom getter for Verein
    public Verein getVerein() {
        return this.vereinMitgliedschaft;
    }

    // Custom setter for Person
    public void setPerson(Person person) {
        this.personMitgliedschaft = person;
    }

    // Custom getter for Person
    public Person getPerson() {
        return this.personMitgliedschaft;
    }
}