package com.kcserver.vermietung.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name="vermietung_briefpapier") @Getter @Setter
public class MandantenBriefpapier {
    @Id private Long id = 1L;
    @Version private Long version;
    @Column(nullable=false, columnDefinition="bytea") private byte[] pdf;
    @Column(columnDefinition="bytea") private byte[] bild;
    @Column(nullable=false, columnDefinition="text") private String gruss;
}
