package com.kcserver.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "unterkunftsart")
public class Unterkunftsart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String bezeichnung;

    @Column(name="preis_pro_person_und_nacht", nullable =false, precision = 10, scale = 2)
    private BigDecimal preisProPersonUndNacht;

    @Column(length = 500)
    private String bemerkung;

    @Builder.Default
    private boolean aktiv = true;
}
