package com.kcserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(
        schema = "public",
        name = "reisekosten_konfiguration"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReisekostenKonfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "pkw_satz",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal pkwSatz;

    @Column(
            name = "mitfahrer_satz",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal mitfahrerSatz;

    @Column(
            name = "anhaenger_satz",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal anhaengerSatz;

    @Column(
            name = "gueltig_von",
            nullable = false
    )
    private LocalDate gueltigVon;

    @Column(
            name = "gueltig_bis"
    )
    private LocalDate gueltigBis;
}
