package com.kcserver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "kik_zuschlag")
public class KikZuschlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gueltig_von", nullable = false)
    private LocalDate gueltigVon;

    @Column(name = "gueltig_bis")
    private LocalDate gueltigBis;

    /**
     * Zusätzlicher Zuschlag pro Tag und Teilnehmer
     * für KiK-zertifizierte Vereine bei FM/JEM.
     */
    @Column(name = "kik_zuschlag", nullable = false, precision = 10, scale = 2)
    private BigDecimal kikZuschlag;

    private String beschluss;
}