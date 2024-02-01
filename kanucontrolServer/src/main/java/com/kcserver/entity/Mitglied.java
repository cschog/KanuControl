package com.kcserver.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@ToString
@NoArgsConstructor
@Table
public class Mitglied {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Other methods...
    @Setter
    @ManyToOne
    @JoinColumn(name = "verein_id")
    private Verein vereinMitgliedschaft;

    @Setter
    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person personMitgliedschaft;

    @Setter
    @Column(name = "funktion")
    private String funktion;

    @Setter
    @Column(name = "haupt_verein")
    private Boolean hauptVerein;

    // Other attributes...


}
