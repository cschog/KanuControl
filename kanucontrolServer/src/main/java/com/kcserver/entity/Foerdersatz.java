package com.kcserver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "foerdersatz")
public class Foerdersatz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate gueltigVon;
    private LocalDate gueltigBis;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal betragProTeilnehmer;

    private String beschluss;
}