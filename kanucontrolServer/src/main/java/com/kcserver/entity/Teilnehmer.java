package com.kcserver.entity;

import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.persistence.converter.TeilnehmerRolleConverter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "teilnehmer",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_teilnehmer_veranstaltung_person",
                        columnNames = {"veranstaltung_id", "person_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teilnehmer extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* =========================
       Beziehungen
       ========================= */

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "veranstaltung_id", nullable = false)
    private Veranstaltung veranstaltung;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    /* =========================
       Rolle in der Veranstaltung
       ========================= */

    @Convert(converter = TeilnehmerRolleConverter.class)
    @Column(nullable = false, length = 1)
    private TeilnehmerRolle rolle;

    /* =========================
       Optional für spätere Versionen
       ========================= */

    @Column(nullable = false)
    private boolean aktiv = true;
}