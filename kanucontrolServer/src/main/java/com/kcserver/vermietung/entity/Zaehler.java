package com.kcserver.vermietung.entity;

import com.kcserver.core.audit.Auditable;
import com.kcserver.vermietung.enumtype.Verbrauchsart;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "zaehler")
public class Zaehler extends Auditable {

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

    @Column(name = "zaehlernummer", length = 100)
    private String zaehlernummer;

    @Column(name = "mengeneinheit", nullable = false, length = 20)
    private String mengeneinheit;

    @Column(nullable = false)
    private boolean aktiv = true;

    @OneToMany(
            mappedBy = "zaehler",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Zaehlerstand> zaehlerstaende = new ArrayList<>();

    @OneToMany(mappedBy = "zaehler")
    private List<Mietbereich> mietbereiche = new ArrayList<>();
}