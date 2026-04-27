package com.kcserver.entity;

import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.enumtype.VeranstaltungTyp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(
        name = "foerdersatz",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_foerdersatz_typ_gueltigkeit",
                        columnNames = {"typ", "gueltig_von"}
                )
        }
)
public class Foerdersatz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VeranstaltungTyp typ;

    @Column(name = "gueltig_von", nullable = false)
    private LocalDate gueltigVon;

    @Column(name = "gueltig_bis")
    private LocalDate gueltigBis;

    /**
     * Grundförderung pro Tag und Teilnehmer.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal foerdersatz;

    /**
     * Maximal zulässige Förderung pro Tag und Teilnehmer.
     */

    private String beschluss;
}