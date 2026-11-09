package com.kcserver.vermietung.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.kcserver.vermietung.service.VorlagenModell.Vorgang;
@Entity @Table(name="vermietung_vorlage", uniqueConstraints=@UniqueConstraint(columnNames={"mietobjekt_id","vorgang"})) @Getter @Setter
public class MietobjektVorlage {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    @Column(name="mietobjekt_id",nullable=false) private Long mietobjektId;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private Vorgang vorgang;
    @Column(nullable=false,columnDefinition="text") private String inhalt;
}
