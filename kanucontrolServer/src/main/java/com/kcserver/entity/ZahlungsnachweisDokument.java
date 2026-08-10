package com.kcserver.entity;

import com.kcserver.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
        name = "zahlungsnachweis_dokument",
        indexes = {
                @Index(
                        name = "idx_zahlungsnachweis_dokument_zn",
                        columnList = "zahlungsnachweis_id"
                )
        }
)
@Getter
@Setter
public class ZahlungsnachweisDokument extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zahlungsnachweis_id", nullable = false)
    private Zahlungsnachweis zahlungsnachweis;

    @Column(nullable = false)
    private Integer reihenfolge;

    @Column(length = 200)
    private String titel;

    @Column(
            name = "original_dateiname",
            nullable = false
    )
    private String originalDateiname;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "dateigroesse", nullable = false)
    private Long dateigroesse;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(nullable = false)
    private byte[] inhalt;

}

