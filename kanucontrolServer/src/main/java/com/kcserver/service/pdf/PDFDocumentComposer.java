package com.kcserver.service.pdf;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.Loader;

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
    public byte[] compose(
            Map<String, byte[]> documents,
            List<A4LayoutPlacement> placements
    ) throws IOException {

        return composeInternal(
                documents,
                placements,
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

    public byte[] composeWithoutFooter(
            Map<String, byte[]> documents,
            List<A4LayoutPlacement> placements
    ) throws IOException {

        return composeInternal(
                documents,
                placements,
                false
        );
    }

    private byte[] composeInternal(
            Map<String, byte[]> documents,
            List<A4LayoutPlacement> placements,
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

            org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject form =
                    forms.get(
                            placement.itemId()
                    );

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
}