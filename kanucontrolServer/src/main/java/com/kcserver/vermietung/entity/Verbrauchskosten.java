package com.kcserver.vermietung.entity;

import com.kcserver.core.audit.Auditable;
import com.kcserver.vermietung.enumtype.Verbrauchsabrechnungsart;
import com.kcserver.vermietung.enumtype.Verbrauchsart;
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
@Table(name = "verbrauchskosten")
public class Verbrauchskosten extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mietobjekt_id", nullable = false)
    private Mietobjekt mietobjekt;

    @Column(nullable = false, length = 100)
    private String bezeichnung;

    @Enumerated(EnumType.STRING)
    @Column(name = "verbrauchsart", nullable = false, length = 20)
    private Verbrauchsart verbrauchsart;

    @Enumerated(EnumType.STRING)
    @Column(name = "abrechnungsart", nullable = false, length = 20)
    private Verbrauchsabrechnungsart abrechnungsart;

    @Column(name = "mengeneinheit", length = 20)
    private String mengeneinheit;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preis;

    @Column(nullable = false)
    private boolean aktiv = true;
}