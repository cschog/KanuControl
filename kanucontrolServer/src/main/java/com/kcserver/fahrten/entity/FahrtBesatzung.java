package com.kcserver.fahrten.entity;

import com.kcserver.core.entity.Teilnehmer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "fahrt_besatzung", uniqueConstraints = {
        @UniqueConstraint(name = "uk_fahrt_teilnehmer", columnNames = {"fahrt_id", "teilnehmer_id"}),
        @UniqueConstraint(name = "uk_fahrtboot_teilnehmer", columnNames = {"fahrt_kanu_id", "teilnehmer_id"})
})
@Getter @Setter
public class FahrtBesatzung {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fahrt_id", nullable = false)
    private Fahrt fahrt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fahrt_kanu_id", nullable = false)
    private FahrtKanu fahrtKanu;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "teilnehmer_id", nullable = false)
    private Teilnehmer teilnehmer;
}
