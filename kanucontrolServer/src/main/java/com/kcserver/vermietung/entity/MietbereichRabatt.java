package com.kcserver.vermietung.entity;

import com.kcserver.core.audit.Auditable;
import com.kcserver.vermietung.enumtype.Rabattart;
import com.kcserver.vermietung.enumtype.RabattwertTyp;
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
        name = "mietbereich_rabatt",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_mietbereich_rabatt_art",
                        columnNames = {"mietbereich_id", "rabattart"}
                )
        }
)
public class MietbereichRabatt extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mietbereich_id", nullable = false)
    private Mietbereich mietbereich;

    @Enumerated(EnumType.STRING)
    @Column(name = "rabattart", nullable = false, length = 20)
    private Rabattart rabattart;

    @Enumerated(EnumType.STRING)
    @Column(name = "rabattwert_typ", nullable = false, length = 20)
    private RabattwertTyp rabattwertTyp;

    @Column(name = "rabattwert", nullable = false, precision = 10, scale = 2)
    private BigDecimal rabattwert;

    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;
}