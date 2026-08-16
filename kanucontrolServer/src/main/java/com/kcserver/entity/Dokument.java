package com.kcserver.entity;

import com.kcserver.enumtype.ReferenzObjekt;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.kcserver.audit.Auditable;


@Entity
@Table(name = "dokument")
@Getter
@Setter
public class Dokument extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beleg_id")
    private AbrechnungBeleg beleg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zahlungsnachweis_id")
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

    /*
     * Bild-/Dokumentgeometrie
     */

    @Column(name = "bild_breite_pixel")
    private Integer bildBreitePixel;

    @Column(name = "bild_hoehe_pixel")
    private Integer bildHoehePixel;

    @Column(name = "dokument_breite_mm")
    private Double dokumentBreiteMm;

    @Column(name = "dokument_hoehe_mm")
    private Double dokumentHoeheMm;

    /*
     * Referenzobjekt zur Maßstabsbestimmung
     */

    @Enumerated(EnumType.STRING)
    @Column(name = "referenz_objekt", length = 50)
    private ReferenzObjekt referenzObjekt;
}
