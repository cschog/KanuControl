package com.kcserver.vermietung.entity;

import com.kcserver.core.audit.Auditable;
import com.kcserver.vermietung.enumtype.Berechnungsart;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "buchung_mietbereich",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_buchung_mietbereich",
                        columnNames = {
                                "buchung_id",
                                "mietbereich_id"
                        }
                )
        }
)
public class BuchungMietbereich extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buchung_id", nullable = false)
    private Buchung buchung;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mietbereich_id", nullable = false)
    private Mietbereich mietbereich;

    @Column(name = "anzahl", nullable = false)
    private Integer anzahl = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "berechnungsart", nullable = false, length = 20)
    private Berechnungsart berechnungsart;

    @Column(name = "vereinbarter_preis", nullable = false, precision = 10, scale = 2)
    private BigDecimal vereinbarterPreis;
}