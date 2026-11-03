package com.kcserver.vermietung.entity;
import com.kcserver.core.audit.Auditable;
import com.kcserver.vermietung.enumtype.Finanzbewegungsart;
import com.kcserver.vermietung.enumtype.Finanzkategorie;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name="buchung_finanzbewegung")
public class BuchungFinanzbewegung extends Auditable {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="buchung_id",nullable=false) private Buchung buchung;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) private Finanzkategorie kategorie;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) private Finanzbewegungsart art;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal betrag;
 @Column(nullable=false) private LocalDate datum;
 @Column(length=500) private String bemerkung;
}
