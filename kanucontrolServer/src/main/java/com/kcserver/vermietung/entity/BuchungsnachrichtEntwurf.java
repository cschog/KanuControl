package com.kcserver.vermietung.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import java.time.*;
@Entity @Table(name="vermietung_nachrichtenentwurf") @Getter @Setter
public class BuchungsnachrichtEntwurf {
 @Column(name="unabhaengig",nullable=false) private boolean unabhaengig;
 @Column(name="aktuelle_fassung",nullable=false) private boolean aktuelleFassung;
 @Column(name="dokument_am") private Instant dokumentAm;
 @Column(name="hinweise",columnDefinition="text") private String hinweise="[]";
 @Id private String id=java.util.UUID.randomUUID().toString();
 @Version private Long version;
 @Column(nullable=false,length=255) private String benutzer;
 @Column(nullable=false,length=255) private String tenant;
 @Column(name="buchung_id",nullable=false) private Long buchungId;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Buchungsstatus quelle;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Buchungsstatus ziel;
 @Column(nullable=false) private LocalDate datum;
 @Column(nullable=false) private Instant ablauf;
 @Column(name="daten_hash",nullable=false,length=64) private String datenHash;
 @Column(nullable=false,length=250) private String betreff="";
 @Column(nullable=false,columnDefinition="text") private String mailtext="";
 @Column(nullable=false,columnDefinition="text") private String routing;
 @Column(name="original_pdf",columnDefinition="bytea") private byte[] originalPdf;
 @Column(columnDefinition="bytea") private byte[] pdf;
 @Column(nullable=false,length=180) private String dateiname;
 @Column(name="extern_am") private Instant externAm;
 @Column(name="ohne_mail",nullable=false) private boolean ohneMail;
 @Column(name="ausgefuehrt",nullable=false) private boolean ausgefuehrt;
 @Column(name="ablesungen",nullable=false,columnDefinition="text") private String ablesungen="[]";
 @Column(name="storno",columnDefinition="text") private String storno;
 @Column(name="rechnung_revision") private Integer rechnungRevision;
 @Column(name="ersetzt_dokument_id") private Long ersetztDokumentId;
 @Column(name="korrekturgrund",nullable=false,length=1000) private String korrekturgrund="";
 @Column(name="versuch_id",length=36) private String versuchId;
}
