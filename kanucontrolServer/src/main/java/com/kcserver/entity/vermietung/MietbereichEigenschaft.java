package com.kcserver.entity.vermietung;

import com.kcserver.audit.Auditable;
import com.kcserver.enumtype.MietbereichEigenschaftTyp;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "mietbereich_eigenschaft")
public class MietbereichEigenschaft extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mietbereich_id", nullable = false)
    private Mietbereich mietbereich;

    @Column(nullable = false)
    private String bezeichnung;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MietbereichEigenschaftTyp typ;

    @Column(nullable = false, length = 1000)
    private String wert;
}