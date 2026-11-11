package com.kcserver.vermietung.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
@Entity @Table(name="vermietung_vorlagenimport") @Getter @Setter
public class VorlagenImport {
    @Id private String id;
    @Column(nullable=false) private String benutzer;
    @Column(nullable=false) private String tenant;
    @Column(nullable=false) private Instant ablauf;
    @Column(columnDefinition="bytea") private byte[] paket;
    @Column(name="paket_hash",nullable=false,length=64) private String paketHash;
    @Column(columnDefinition="text") private String plan;
    @Column(name="ziel_hash",length=64) private String zielHash;
    private boolean geprueft;
    private boolean uebernommen;
}
