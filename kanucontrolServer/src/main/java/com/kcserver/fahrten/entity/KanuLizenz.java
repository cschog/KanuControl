package com.kcserver.fahrten.entity;

import com.kcserver.core.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "kanu_lizenz")
@Getter @Setter
public class KanuLizenz extends Auditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "kanu_id", nullable = false)
    private Kanu kanu;

    @Column(name = "lizenznummer", nullable = false, length = 120)
    private String lizenznummer;
    @Column(name = "gueltig_von", nullable = false)
    private LocalDate gueltigVon;
    @Column(name = "gueltig_bis", nullable = false)
    private LocalDate gueltigBis;
    @Column(precision = 10, scale = 2)
    private BigDecimal kosten;
}
