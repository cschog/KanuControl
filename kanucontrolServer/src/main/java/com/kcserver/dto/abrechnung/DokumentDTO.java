package com.kcserver.dto.abrechnung;

import com.kcserver.enumtype.ReferenzObjekt;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class DokumentDTO {

    private Long id;

    /**
     * Reihenfolge innerhalb des Besitzers.
     */
    private Integer reihenfolge;

    /**
     * Optionaler Titel.
     */
    private String titel;

    /**
     * Ursprünglicher Dateiname.
     */
    private String originalDateiname;

    /**
     * MIME-Type.
     */
    private String mimeType;

    /**
     * Dateigröße in Byte.
     */
    private Long dateigroesse;

    /**
     * Bildbreite in Pixeln.
     */
    private Integer bildBreitePixel;

    /**
     * Bildhöhe in Pixeln.
     */
    private Integer bildHoehePixel;

    /**
     * Erkannte tatsächliche Dokumentbreite in Millimetern.
     */
    private Double dokumentBreiteMm;

    /**
     * Erkannte tatsächliche Dokumenthöhe in Millimetern.
     */
    private Double dokumentHoeheMm;

    /**
     * Referenzobjekt, das zur Maßstabsbestimmung verwendet wurde.
     */
    private ReferenzObjekt referenzObjekt;

    private Instant createdAt;
}