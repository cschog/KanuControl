package com.kcserver.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Entity(name = "mitglied")
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
    @ManyToOne
    @JoinColumn(name = "verein_id", nullable = false)
    private Verein vereinMitgliedschaft;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    private Person personMitgliedschaft;

    @Column(name = "funktion")
    private String funktion;

    @NotNull
    @Column(name = "haupt_verein")
    private Boolean hauptVerein;

    public
    Mitglied(Verein vereinMitgliedschaft, Person personMitgliedschaft, String funktion, Boolean hauptVerein) {
        this.vereinMitgliedschaft = vereinMitgliedschaft;
        this.personMitgliedschaft = personMitgliedschaft;
        this.funktion = funktion;
        this.hauptVerein = hauptVerein;
    }
}