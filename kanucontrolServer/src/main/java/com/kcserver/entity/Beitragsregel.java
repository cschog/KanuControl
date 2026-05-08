package com.kcserver.entity;

import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.persistence.converter.TeilnehmerRolleConverter;
import jakarta.persistence.*;
import com.kcserver.audit.Auditable;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Table(name = "beitragsregel")
@Getter
@Setter
@Entity
public class Beitragsregel extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer sortierung;

    @Column(name = "alter_bis")
    private Integer alterBis;

    @Convert(converter = TeilnehmerRolleConverter.class)
    @Column(length = 1)
    private TeilnehmerRolle rolle;

    private BigDecimal beitrag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beitragsstruktur_id")
    private Beitragsstruktur struktur;
}
