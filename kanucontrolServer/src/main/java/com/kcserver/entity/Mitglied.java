package com.kcserver.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity(name = "mitglied")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table
public class Mitglied {

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
    Mitglied(Verein vereinMitgliedschaft, Person personMitgliedschaft, String funktion, Boolean hauptVerein) {
        this.vereinMitgliedschaft = vereinMitgliedschaft;
        this.personMitgliedschaft = personMitgliedschaft;
        this.funktion = funktion;
        this.hauptVerein = hauptVerein;
    }
}