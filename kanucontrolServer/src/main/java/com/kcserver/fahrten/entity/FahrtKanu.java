package com.kcserver.fahrten.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "fahrt_kanu", uniqueConstraints = @UniqueConstraint(name = "uk_fahrt_kanu", columnNames = {"fahrt_id", "kanu_id"}))
@Getter @Setter
public class FahrtKanu {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fahrt_id", nullable = false)
    private Fahrt fahrt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "kanu_id", nullable = false)
    private Kanu kanu;

    @Column(name = "lizenz_erforderlich", nullable = false)
    private boolean lizenzErforderlich;
}
