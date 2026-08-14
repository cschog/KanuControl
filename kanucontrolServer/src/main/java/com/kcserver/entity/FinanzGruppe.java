package com.kcserver.entity;

import com.kcserver.enumtype.FinanzgruppeTyp;
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

    @Column(nullable = false, length = 20)
    private String kuerzel;

    @Builder.Default
    @Column(nullable = false)
    private boolean system = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FinanzgruppeTyp typ;

    /* =========================================================
       VERANSTALTUNG
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