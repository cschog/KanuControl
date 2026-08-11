package com.kcserver.service.pdf;

/**
 * Ergebnis einer A4-Layoutplanung.
 *
 * x / y / width / height beschreiben die Ausgabe
 * auf der A4-Seite.
 *
 * rotation:
 *   0  = normal
 *   90 = um 90° gedreht
 *
 * sourceY / sourceHeight werden benötigt, wenn ein
 * Dokument auf mehrere Seiten aufgeteilt werden muss.
 */
public record A4LayoutPlacement(
        String itemId,

        int pageNumber,

        float x,
        float y,
        float width,
        float height,

        float sourceY,
        float sourceHeight,

        int rotation,

        boolean continued
) {
}