package com.kcserver.vermietung.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name="vermietung_aenderung") @Getter @Setter @org.hibernate.annotations.Immutable
public class VermietungAenderung {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private java.time.Instant zeitpunkt;
 @Column(nullable=false) private String benutzer;
 @Column(nullable=false) private String aktion;
 @Column(nullable=false,columnDefinition="text") private String daten;
}
