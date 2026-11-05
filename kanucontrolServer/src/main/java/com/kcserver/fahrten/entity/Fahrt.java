package com.kcserver.fahrten.entity;

import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.core.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "fahrt")
@Getter @Setter
public class Fahrt extends Auditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "veranstaltung_id", nullable = false)
    private Veranstaltung veranstaltung;

    @Column(nullable = false)
    private LocalDate datum;
    private LocalTime beginnZeit;
    private LocalTime endeZeit;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "gewaesser_id", nullable = false)
    private Gewaesser gewaesser;

    @Column(name = "startort", length = 160)
    private String startort;
    @Column(name = "zielort", length = 160)
    private String zielort;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal kilometer;

    @Column(name = "streckenangabe", length = 500)
    private String streckenangabe;
    @Column(length = 4000)
    private String bemerkung;
}
