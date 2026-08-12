package com.kcserver.service.pdf;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import javax.imageio.ImageIO;
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

    /**
     * Erstellt aus den Quell-PDFs und den Layout-Platzierungen
     * ein gemeinsames A4-PDF.
     *
     * @param documents Quell-PDFs, keyed by itemId
     * @param placements Layout-Platzierungen
     * @return fertiges A4-PDF
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
    private void placeForm(
            PDDocument target,
            org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject form,
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

        float scaleX =
                placement.width()
                        / sourceWidth;

        float scaleY =
                placement.height()
                        / sourceHeight;

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
            if (placement.rotation() == 90) {

                /*
                 * 90° Drehung.
                 *
                 * Nach der Transformation liegt die
                 * komplette Quellseite innerhalb der
                 * Zielrechtecks.
                 */
                matrix = new Matrix(
                        0,
                        scaleY,
                        -scaleX,
                        0,
                        placement.x()
                                + placement.width(),
                        placement.y()
                );

            } else {

                matrix = new Matrix(
                        scaleX,
                        0,
                        0,
                        scaleY,
                        placement.x(),
                        placement.y()
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

        BufferedImage bufferedImage =
                ImageIO.read(
                        new ByteArrayInputStream(imageBytes)
                );

        if (bufferedImage == null) {
            throw new IOException(
                    "Bild konnte nicht gelesen werden: "
                            + placement.itemId()
            );
        }

        PDImageXObject image =
                LosslessFactory.createFromImage(
                        target,
                        bufferedImage
                );

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

            if (placement.rotation() == 90) {

                matrix = new Matrix(
                        0,
                        placement.height(),
                        -placement.width(),
                        0,
                        placement.x() + placement.width(),
                        placement.y()
                );

            } else {

                matrix = new Matrix(
                        placement.width(),
                        0,
                        0,
                        placement.height(),
                        placement.x(),
                        placement.y()
                );
            }

            content.transform(matrix);

            content.drawImage(
                    image,
                    0,
                    0,
                    1,
                    1
            );
        }
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