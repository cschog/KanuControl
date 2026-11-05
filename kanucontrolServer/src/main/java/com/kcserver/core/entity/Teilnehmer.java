package com.kcserver.core.entity;

import com.kcserver.kjfp.entity.FinanzGruppe;

import com.kcserver.core.audit.Auditable;
import com.kcserver.core.entity.Person;
import com.kcserver.core.enumtype.TeilnehmerRolle;
import com.kcserver.core.converter.TeilnehmerRolleConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

    // Shared identity. Existing KJFP associations retain their mappings for API/DB compatibility.

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finanz_gruppe_id")
    private FinanzGruppe finanzGruppe;

    /* =========================
       Rolle in der Veranstaltung
       ========================= */

    @Convert(converter = TeilnehmerRolleConverter.class)
    @Column(length = 1)
    private TeilnehmerRolle rolle;

    @Column(name = "individueller_beitrag", precision = 10, scale = 2)
    private BigDecimal individuellerBeitrag;

    @Column(name="kjfp_zugeordnet", nullable=false)
    private boolean kjfpZugeordnet = true;
}