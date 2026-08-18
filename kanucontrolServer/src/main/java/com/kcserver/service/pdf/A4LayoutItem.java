package com.kcserver.service.pdf;

import com.kcserver.enumtype.PdfDocumentDensity;
import com.kcserver.enumtype.ReferenzObjekt;

import java.util.Objects;

/**
 * Beschreibt ein Dokument für die A4-Layoutplanung.
 *
 * width / height sind die ursprünglichen Abmessungen
 * des Dokuments in PDF-Punkten.
 *
 * referenzObjekt beschreibt das physische Papierformat
 * und damit die gewünschte Dokumentausrichtung.
 *
 * Die Engine selbst kennt den eigentlichen Dokumentinhalt nicht.
 */
public record A4LayoutItem(
        String id,
        float width,
        float height,
        PdfDocumentDensity density,
        ReferenzObjekt referenzObjekt
) {

    public A4LayoutItem {

        Objects.requireNonNull(
                id,
                "id darf nicht null sein."
        );

        Objects.requireNonNull(
                density,
                "density darf nicht null sein."
        );

        Objects.requireNonNull(
                referenzObjekt,
                "referenzObjekt darf nicht null sein."
        );

        if (width <= 0) {
            throw new IllegalArgumentException(
                    "width muss größer als 0 sein."
            );
        }

        if (height <= 0) {
            throw new IllegalArgumentException(
                    "height muss größer als 0 sein."
            );
        }
    }

    public float aspectRatio() {
        return width / height;
    }
}