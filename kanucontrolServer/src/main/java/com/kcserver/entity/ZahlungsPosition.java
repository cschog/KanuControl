package com.kcserver.entity;

import jakarta.persistence.*;
import com.kcserver.audit.Auditable;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "zahlungs_position")
public class ZahlungsPosition extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zahlungsnachweis_id", nullable = false)
    private Zahlungsnachweis zahlungsnachweis;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teilnehmer_id", nullable = false)
    private Teilnehmer teilnehmer;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal betrag;
}
