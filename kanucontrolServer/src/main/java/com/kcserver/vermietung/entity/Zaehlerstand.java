package com.kcserver.vermietung.entity;

import com.kcserver.core.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "zaehlerstand")
public class Zaehlerstand extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zaehler_id", nullable = false)
    private Zaehler zaehler;

    @Column(name = "ablesedatum", nullable = false)
    private LocalDateTime ablesedatum;

    @Column(
            name = "zaehlerstand",
            nullable = false,
            precision = 15,
            scale = 3
    )
    private BigDecimal zaehlerstand;

    @Column(length = 1000)
    private String bemerkung;
}