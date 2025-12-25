package com.kcserver.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "mitglied",
        uniqueConstraints = @UniqueConstraint(columnNames = {"person_id", "verein_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Mitglied extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "verein_id", nullable = false)
    private Verein verein;

    @Column(name = "funktion")
    private String funktion;

    @Column(name = "haupt_verein", nullable = false)
    private Boolean hauptVerein;
}