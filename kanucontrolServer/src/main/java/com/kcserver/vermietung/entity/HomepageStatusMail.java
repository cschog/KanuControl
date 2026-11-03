package com.kcserver.vermietung.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

/** Versandauftrag als unveränderlicher Schnappschuss einer Statusentscheidung. */
@Entity @Getter @Setter
@Table(name = "homepage_status_mail")
public class HomepageStatusMail {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;
    @Column(name = "mietobjekt_id", nullable = false)
    private Long mietobjektId;
    @Column(name = "buchung_id")
    private Long buchungId;
    @Column(name = "buchungsnummer", nullable = false, length = 64)
    private String buchungsnummer;
    @Column(nullable = false, length = 20)
    private String status;
    @Column(nullable = false, length = 254)
    private String empfaenger;
    @Column(name = "reply_to", length = 254)
    private String replyTo;
    @Column(nullable = false, length = 300)
    private String betreff;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "quittiert_am")
    private Instant quittiertAm;
}
