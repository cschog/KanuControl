package com.kcserver.entity;

import jakarta.persistence.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.math.BigDecimal;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "verpflegungsmodell")
public class Verpflegungsmodell {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String bezeichnung;

    @Column(name="preis_pro_person_und_tag", nullable = false, precision = 10, scale = 2)
    private BigDecimal preisProPersonUndTag;

    @Column(length = 500)
    private String bemerkung;

    private boolean aktiv = true;
}
