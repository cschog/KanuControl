package com.kcserver.vermietung.entity;

import com.kcserver.core.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity @Table(name = "mietobjekt_sperrzeit") @Getter @Setter
public class MietobjektSperrzeit extends Auditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mietobjekt_id", nullable = false)
    private Mietobjekt mietobjekt;
    @Column(nullable = false) private LocalDateTime von;
    @Column(nullable = false) private LocalDateTime bis;
    @Column(nullable = false) private boolean ganztaegig;
    @Column(nullable = false, length = 500) private String grund;
}
