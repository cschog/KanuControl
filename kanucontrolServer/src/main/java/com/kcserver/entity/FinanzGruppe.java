package com.kcserver.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "finanz_gruppe",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_finanzgruppe_veranstaltung_kuerzel",
                columnNames = {"veranstaltung_id", "kuerzel"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanzGruppe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* =========================================================
       KUERZEL
       ========================================================= */

    @Column(nullable = false, length = 20)
    private String kuerzel;

    /* =========================================================
       RELATION ZUR VERANSTALTUNG
       ========================================================= */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veranstaltung_id", nullable = false)
    private Veranstaltung veranstaltung;

    /* =========================================================
       TEILNEHMER
       ========================================================= */

    @OneToMany(
            mappedBy = "finanzGruppe",
            cascade = CascadeType.ALL,
            orphanRemoval = false
    )
    @Builder.Default
    private List<Teilnehmer> teilnehmer = new ArrayList<>();
}