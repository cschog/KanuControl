package com.kcserver.vermietung.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
@Entity @Table(name="entschaedigung_katalog") @Getter @Setter
public class EntschaedigungKatalog {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="mietobjekt_id",nullable=false) private Mietobjekt mietobjekt;
 @Column(nullable=false,length=250) private String bezeichnung;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal einzelpreis;
 @Column(nullable=false) private int sortierung;
 @Column(nullable=false) private boolean aktiv=true;
 @Version private Long version;
}
