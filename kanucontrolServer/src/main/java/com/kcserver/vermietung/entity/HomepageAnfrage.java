package com.kcserver.vermietung.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity @Getter @Setter
@Table(name = "homepage_anfrage", uniqueConstraints = @UniqueConstraint(
        name = "uk_homepage_anfrage", columnNames = {"mietobjekt_id", "anfrage_id"}))
public class HomepageAnfrage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "mietobjekt_id", nullable = false)
    private Long mietobjektId;
    @Column(name = "anfrage_id", nullable = false, length = 36)
    private String anfrageId;
    @Column(name = "payload_hash", nullable = false, length = 64)
    private String payloadHash;
    @Column(name = "antwort_json", nullable = false, columnDefinition = "TEXT")
    private String antwortJson;
    @Column(name = "hinweis_stand", nullable = false, columnDefinition = "TEXT")
    private String hinweisStand;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
