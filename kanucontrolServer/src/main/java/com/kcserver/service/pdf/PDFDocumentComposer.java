package com.kcserver.service.pdf;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PDFDocumentComposer {

    private final PDFLayoutService layoutService;

    private static final int IMAGE_DPI = 180;
    private static final float JPEG_QUALITY = 0.85f;

    /**
     * Erstellt aus den Quell-PDFs und den Layout-Platzierungen
     * ein gemeinsames A4-PDF.
     *
     */
    private static final PDType1Font FOOTER_FONT =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_BOLD
            );

    public byte[] compose(
            Map<String, byte[]> documents,
            List<A4LayoutPlacement> placements
    ) throws IOException {

        return composeInternal(
                documents,
                placements,
                null,
                true
        );
    }



    /**
     * Platziert eine importierte PDF-Seite auf einer A4-Seite.
     */
    /**
     * Platziert eine importierte PDF-Seite auf einer A4-Seite.
     *
     * Die Rotation wird ausschließlich durch die
     * A4LayoutPlacement vorgegeben.
     */
    /**
     * Platziert eine importierte PDF-Seite auf einer A4-Seite.
     *
     * Die Rotation wird grundsätzlich durch die
     * A4LayoutPlacement vorgegeben.
     *
     * Zusätzlich werden Querformat-PDFs um 180° gedreht.
     * Dadurch bleibt die bestehende 90°-Rotation der Layout-Engine
     * erhalten und wird bei Querformat-PDFs entsprechend ergänzt.
     */
    private void placeForm(
            PDDocument target,
            PDFormXObject form,
            A4LayoutPlacement placement
    ) throws IOException {

        int pageIndex =
                placement.pageNumber() - 1;

        if (pageIndex < 0
                || pageIndex >= target.getNumberOfPages()) {

            throw new IllegalArgumentException(
                    "Ungültige Seitennummer: "
                            + placement.pageNumber()
            );
        }

        PDPage targetPage =
                target.getPage(pageIndex);

        PDRectangle bbox =
                form.getBBox();

        float sourceWidth =
                bbox.getWidth();

        float sourceHeight =
                bbox.getHeight();

        /*
         * Die Layout-Engine hat bereits entschieden,
         * ob das PDF um 90° gedreht werden soll.
         *
         * Für PDFs mit Layout-Rotation 90° wollen wir
         * zusätzlich 180° drehen:
         *
         *   90° + 180° = 270°
         */

        int rotation =
                ((placement.rotation() % 360) + 360) % 360;

        if (rotation == 90) {
            rotation = 270;
        }

        /*
         * =========================================================
         * Effektive Dokumentgröße
         * =========================================================
         *
         * Bei 90° und 270° werden Breite und Höhe vertauscht.
         * Bei 0° und 180° bleiben sie unverändert.
         */
        boolean swapDimensions =
                rotation == 90
                        || rotation == 270;

        float effectiveWidth =
                swapDimensions
                        ? sourceHeight
                        : sourceWidth;

        float effectiveHeight =
                swapDimensions
                        ? sourceWidth
                        : sourceHeight;

        /*
         * =========================================================
         * Skalierung
         * =========================================================
         */
        float scale =
                Math.min(
                        placement.width()
                                / effectiveWidth,

                        placement.height()
                                / effectiveHeight
                );

        /*
         * Tatsächliche Ausgabegröße.
         */
        float outputWidth =
                effectiveWidth * scale;

        float outputHeight =
                effectiveHeight * scale;

        /*
         * Innerhalb des Layout-Rechtecks zentrieren.
         */
        float x =
                placement.x()
                        + (placement.width()
                        - outputWidth) / 2f;

        float y =
                placement.y()
                        + (placement.height()
                        - outputHeight) / 2f;

        try (
                PDPageContentStream content =
                        new PDPageContentStream(
                                target,
                                targetPage,
                                PDPageContentStream.AppendMode.APPEND,
                                true,
                                true
                        )
        ) {

            Matrix matrix;

            switch (rotation) {

                /*
                 * Keine Rotation
                 */
                case 0:

                    matrix =
                            new Matrix(
                                    scale,
                                    0,
                                    0,
                                    scale,
                                    x,
                                    y
                            );

                    break;

                /*
                 * 90° Rotation
                 *
                 * Entspricht deiner bisherigen Implementierung.
                 */
                case 90:

                    matrix =
                            new Matrix(
                                    0,
                                    -scale,
                                    scale,
                                    0,
                                    x,
                                    y + outputHeight
                            );

                    break;

                /*
                 * 180° Rotation
                 *
                 * Wichtig:
                 * Breite/Höhe werden NICHT vertauscht.
                 */
                case 180:

                    matrix =
                            new Matrix(
                                    -scale,
                                    0,
                                    0,
                                    -scale,
                                    x + outputWidth,
                                    y + outputHeight
                            );

                    break;

                /*
                 * 270° Rotation
                 *
                 * Entspricht 90° in Gegenrichtung.
                 */
                case 270:

                    matrix =
                            new Matrix(
                                    0,
                                    scale,
                                    -scale,
                                    0,
                                    x + outputWidth,
                                    y
                            );

                    break;

                default:

                    throw new IllegalArgumentException(
                            "Nicht unterstützte Rotation: "
                                    + rotation
                    );
            }

            content.transform(matrix);

            content.drawForm(form);
        }
    }

    private void placeImage(
            PDDocument target,
            byte[] imageBytes,
            A4LayoutPlacement placement
    ) throws IOException {

        int pageIndex =
                placement.pageNumber() - 1;

        if (pageIndex < 0
                || pageIndex >= target.getNumberOfPages()) {

            throw new IllegalArgumentException(
                    "Ungültige Seitennummer: "
                            + placement.pageNumber()
            );
        }

        PDPage targetPage =
                target.getPage(pageIndex);

        BufferedImage original =
                ImageIO.read(
                        new ByteArrayInputStream(imageBytes)
                );

        if (original == null) {

            throw new IOException(
                    "Bild konnte nicht gelesen werden: "
                            + placement.itemId()
            );
        }

        /*
         * =========================================================
         * Benötigte Pixelgröße aus der PDF-Größe berechnen.
         * =========================================================
         */

        boolean rotate =
                placement.rotation() == 90;

        float displayWidth =
                rotate
                        ? placement.height()
                        : placement.width();

        float displayHeight =
                rotate
                        ? placement.width()
                        : placement.height();

        int targetWidth =
                Math.max(
                        1,
                        Math.round(
                                displayWidth
                                        / 72f
                                        * IMAGE_DPI
                        )
                );

        int targetHeight =
                Math.max(
                        1,
                        Math.round(
                                displayHeight
                                        / 72f
                                        * IMAGE_DPI
                        )
                );

        /*
         * Niemals ein Bild vergrößern.
         */
        targetWidth =
                Math.min(
                        targetWidth,
                        original.getWidth()
                );

        targetHeight =
                Math.min(
                        targetHeight,
                        original.getHeight()
                );

        /*
         * Seitenverhältnis des Originalbildes erhalten.
         */
        double imageScale =
                Math.min(
                        (double) targetWidth
                                / original.getWidth(),

                        (double) targetHeight
                                / original.getHeight()
                );

        int scaledWidth =
                Math.max(
                        1,
                        (int) Math.round(
                                original.getWidth()
                                        * imageScale
                        )
                );

        int scaledHeight =
                Math.max(
                        1,
                        (int) Math.round(
                                original.getHeight()
                                        * imageScale
                        )
                );

        BufferedImage image =
                original;

        /*
         * Nur skalieren, wenn es tatsächlich nötig ist.
         */
        if (scaledWidth != original.getWidth()
                || scaledHeight != original.getHeight()) {

            image =
                    new BufferedImage(
                            scaledWidth,
                            scaledHeight,
                            BufferedImage.TYPE_INT_RGB
                    );

            Graphics2D graphics =
                    image.createGraphics();

            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC
            );

            graphics.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );

            graphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            graphics.drawImage(
                    original,
                    0,
                    0,
                    scaledWidth,
                    scaledHeight,
                    null
            );

            graphics.dispose();
        }

        /*
         * =========================================================
         * JPEG statt verlustfreier Speicherung.
         * =========================================================
         */

        PDImageXObject pdfImage =
                JPEGFactory.createFromImage(
                        target,
                        image,
                        JPEG_QUALITY
                );

        /*
         * =========================================================
         * Tatsächliche Ausgabegröße unter Beibehaltung
         * des Seitenverhältnisses bestimmen.
         * =========================================================
         */

        float sourceWidth =
                original.getWidth();

        float sourceHeight =
                original.getHeight();

        float effectiveWidth =
                rotate
                        ? sourceHeight
                        : sourceWidth;

        float effectiveHeight =
                rotate
                        ? sourceWidth
                        : sourceHeight;

        float scale =
                Math.min(
                        placement.width() / effectiveWidth,
                        placement.height() / effectiveHeight
                );

        float outputWidth =
                effectiveWidth * scale;

        float outputHeight =
                effectiveHeight * scale;

        float x =
                placement.x()
                        + (placement.width() - outputWidth) / 2f;

        float y =
                placement.y()
                        + (placement.height() - outputHeight) / 2f;


        /*
         * =========================================================
         * Auf Zielseite platzieren
         * =========================================================
         */

        try (
                PDPageContentStream content =
                        new PDPageContentStream(
                                target,
                                targetPage,
                                PDPageContentStream.AppendMode.APPEND,
                                true,
                                true
                        )
        ) {

            Matrix matrix;

            if (rotate) {

                matrix =
                        new Matrix(
                                0,
                                -scale,
                                scale,
                                0,
                                x,
                                y + outputHeight
                        );

            } else {

                matrix =
                        new Matrix(
                                scale,
                                0,
                                0,
                                scale,
                                x,
                                y
                        );
            }

            content.transform(matrix);

            content.drawImage(
                    pdfImage,
                    0,
                    0,
                    sourceWidth,
                    sourceHeight
            );
        }
        /*
         * Originalbild möglichst schnell freigeben.
         */
        if (image != original) {
            image.flush();
        }

        original.flush();
    }

    public byte[] composeWithoutFooter(
            Map<String, byte[]> documents,
            List<A4LayoutPlacement> placements,
            Map<String, String> belegNummern
    ) throws IOException {

        return composeInternal(
                documents,
                placements,
                belegNummern,
                false
        );
    }

    private boolean isPdf(byte[] content) {

        return content != null
                && content.length >= 5
                && content[0] == '%'
                && content[1] == 'P'
                && content[2] == 'D'
                && content[3] == 'F'
                && content[4] == '-';
    }

    private byte[] composeInternal(
            Map<String, byte[]> documents,
            List<A4LayoutPlacement> placements,
            Map<String, String> belegNummern,
            boolean addFooter
    ) throws IOException {

     if (documents == null || documents.isEmpty()) {
        return new byte[0];
    }

        if (placements == null || placements.isEmpty()) {
        return new byte[0];
    }

    /*
     * Ziel-PDF
     */
        try (PDDocument target = new PDDocument()) {

        /*
         * Eine Seite pro tatsächlich benötigter A4-Seite.
         */
        int pageCount =
                placements.stream()
                        .mapToInt(
                                A4LayoutPlacement::pageNumber
                        )
                        .max()
                        .orElse(0);

        for (int i = 0; i < pageCount; i++) {

            target.addPage(
                    new PDPage(
                            PDFLayoutService.PAGE_SIZE
                    )
            );
        }

        LayerUtility layerUtility =
                new LayerUtility(target);

        /*
         * Bereits importierte Quellseiten.
         *
         * Ein Dokument kann mehrfach verwendet werden,
         * daher cachen wir die importierte Form.
         */
        Map<String, org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject> forms =
                new HashMap<>();

        for (A4LayoutPlacement placement : placements) {

            byte[] sourcePdf =
                    documents.get(
                            placement.itemId()
                    );

            if (sourcePdf == null) {
                throw new IllegalArgumentException(
                        "Kein Quelldokument für itemId: "
                                + placement.itemId()
                );
            }

            if (isPdf(sourcePdf)) {

                PDFormXObject form =
                        forms.get(placement.itemId());

                if (form == null) {

                    try (
                            PDDocument source =
                                    Loader.loadPDF(sourcePdf)
                    ) {

                        if (source.getNumberOfPages() == 0) {
                            throw new IllegalArgumentException(
                                    "Quelldokument enthält keine Seite: "
                                            + placement.itemId()
                            );
                        }

                        PDPage sourcePage =
                                source.getPage(0);

                        form =
                                layerUtility.importPageAsForm(
                                        source,
                                        0
                                );

                        forms.put(
                                placement.itemId(),
                                form
                        );
                    }
                }

                placeForm(
                        target,
                        form,
                        placement
                );

            } else {

                placeImage(
                        target,
                        sourcePdf,
                        placement
                );

            }
            /*
             * Belegnummer über das Dokument legen.
             */
            drawBelegNummer(
                    target,
                    placement,
                    belegNummern);
        }

            /*
             * Footer nur dann hinzufügen, wenn dieses
             * Dokument bereits das endgültige PDF ist.
             */
            if (addFooter) {
                layoutService.addFooter(target);
            }

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            target.save(output);

            return output.toByteArray();
    }
}
    private void drawBelegNummer(
            PDDocument target,
            A4LayoutPlacement placement,
            Map<String, String> belegNummern
    ) throws IOException {

        if (belegNummern == null) {
            return;
        }

        String belegNummer =
                belegNummern.get(
                        placement.itemId()
                );

        if (belegNummer == null
                || belegNummer.isBlank()) {
            return;
        }

        int pageIndex =
                placement.pageNumber() - 1;

        if (pageIndex < 0
                || pageIndex >= target.getNumberOfPages()) {
            return;
        }

        PDPage page =
                target.getPage(pageIndex);

        /*
         * Position:
         *
         * leicht innerhalb des Dokumentes,
         * oben links.
         */
        float x =
                placement.x() + 6f;

        float y =
                placement.y()
                        + placement.height()
                        - 16f;

        float fontSize = 8f;

        try (
                PDPageContentStream content =
                        new PDPageContentStream(
                                target,
                                page,
                                PDPageContentStream.AppendMode.APPEND,
                                true,
                                true
                        )
        ) {

            /*
             * Weißer Hintergrund für gute Lesbarkeit.
             */
            content.setNonStrokingColor(
                    1f,
                    1f,
                    1f
            );

            content.addRect(
                    x - 3f,
                    y - 3f,
                    48f,
                    13f
            );

            content.fill();

            /*
             * Belegnummer.
             */
            content.beginText();

            content.setFont(
                    FOOTER_FONT,
                    fontSize
            );

            content.setNonStrokingColor(
                    0f,
                    0f,
                    0f
            );

            content.newLineAtOffset(
                    x,
                    y
            );

            content.showText(
                    "Beleg " + belegNummer
            );

            content.endText();
        }
    }
}