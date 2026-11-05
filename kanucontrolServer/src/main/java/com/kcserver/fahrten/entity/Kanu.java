package com.kcserver.fahrten.entity;

import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.core.audit.Auditable;
import com.kcserver.core.entity.Person;
import com.kcserver.fahrten.enumtype.KanuTyp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "kanu")
@Getter @Setter
public class Kanu extends Auditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String kennzeichnung;

    @Column(nullable = false, length = 160)
    private String bezeichnung;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KanuTyp typ;

    @Column(name = "platzanzahl", nullable = false)
    private int platzanzahl;

    @Column(nullable = false)
    private boolean aktiv = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veranstaltung_id")
    private Veranstaltung veranstaltung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eigentuermer_id")
    private Person eigentuemer;
}
