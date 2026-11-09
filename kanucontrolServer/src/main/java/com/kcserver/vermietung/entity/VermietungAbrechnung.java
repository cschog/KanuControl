package com.kcserver.vermietung.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
@Entity @Table(name="vermietung_abrechnung") @Getter @Setter
public class VermietungAbrechnung {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="buchung_id",nullable=false) private Buchung buchung;
 @Column(nullable=false) private int revision;
 @Column(nullable=false) private Instant erstellt;
 @Column(nullable=false,length=1000) private String korrekturgrund;
 @Column(nullable=false,columnDefinition="bytea") private byte[] pdf;
}
