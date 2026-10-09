package com.kcserver.vermietung.entity;

import com.kcserver.core.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

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
}