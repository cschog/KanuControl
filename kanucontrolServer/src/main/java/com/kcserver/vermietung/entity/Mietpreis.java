package com.kcserver.vermietung.entity;

import com.kcserver.core.audit.Auditable;
import com.kcserver.vermietung.enumtype.Buchungsquelle;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "mietpreis",
        indexes = {
                @Index(
                        name = "idx_mietpreis_bereich_gueltig",
                        columnList = "mietbereich_id, gueltig_ab"
                )
        }
)
public class Mietpreis extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mietbereich_id", nullable = false)
    private Mietbereich mietbereich;

    @Enumerated(EnumType.STRING)
    @Column(name = "buchungsquelle", nullable = false, length = 20)
    private Buchungsquelle buchungsquelle;

    @Column(name = "gueltig_ab", nullable = false)
    private LocalDate gueltigAb;

    @Column(name = "preis", nullable = false, precision = 10, scale = 2)
    private BigDecimal preis;

    @Column(length = 1000)
    private String bemerkung;
}