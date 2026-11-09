package com.kcserver.vermietung.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
@Entity @Table(name="vermietung_vorlagenversand") @Getter @Setter
public class VorlagenVersand {
    @Id private String id = java.util.UUID.randomUUID().toString();
    @Column(name="buchung_id",nullable=false) private Long buchungId;
    @Column(name="mietobjekt_id",nullable=false) private Long mietobjektId;
    @Column(name="erstellt_am",nullable=false) private Instant erstelltAm = Instant.now();
    @Column(name="smtp_am") private Instant smtpAm;
    @Column(nullable=false,length=30) private String vorgang;
    @Column(nullable=false,columnDefinition="bytea") private byte[] pdf;
    @Column(nullable=false,columnDefinition="text") private String metadaten;
    @Column(nullable=false,length=30) private String ergebnis="VORBEREITET";
    @Column(columnDefinition="text") private String fehler;
}
