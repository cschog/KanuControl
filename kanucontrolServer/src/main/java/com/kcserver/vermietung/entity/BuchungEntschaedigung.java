package com.kcserver.vermietung.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
@Entity @Table(name="buchung_entschaedigung") @Getter @Setter
public class BuchungEntschaedigung {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="buchung_id",nullable=false) private Buchung buchung;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="katalog_id") private EntschaedigungKatalog katalog;
 @Column(nullable=false,length=250) private String bezeichnung;
 public enum Anlass { BESCHAEDIGUNG, VERLUST }
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Anlass anlass;
 @Column(nullable=false) private Integer anzahl;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal einzelpreis;
 @Column(precision=10,scale=2) private BigDecimal katalogpreis;
 @Column(length=1000) private String bemerkung;
 @Column(length=1000) private String preisbegruendung;
 @Column(nullable=false) private int sortierung;
 @Transient public BigDecimal getGesamtbetrag(){return einzelpreis.multiply(BigDecimal.valueOf(anzahl)).setScale(2);}
}
