package com.kcserver.service.pdf;

import java.util.Objects;

/**
 * Beschreibt ein Dokument für die A4-Layoutplanung.
 *
 * width / height sind die ursprünglichen Abmessungen
 * des Dokuments in PDF-Punkten.
 *
 * Die Engine selbst kennt den eigentlichen Dokumentinhalt nicht.
 */
public record A4LayoutItem(
        String id,
        float width,
        float height
) {

    public A4LayoutItem {
        Objects.requireNonNull(id, "id darf nicht null sein.");

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