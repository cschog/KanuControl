package com.kcserver.entity;

import com.kcserver.audit.Auditable;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "gebuehr_raster")
public class GebuehrRaster extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Veranstaltung veranstaltung;

    private Integer alterVon;
    private Integer alterBis;

    private BigDecimal betrag;

    private Boolean nurMitNachweis; // z.B. Ausbildung/Studium

}
