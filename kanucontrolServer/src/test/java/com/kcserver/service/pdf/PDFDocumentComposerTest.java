package com.kcserver.service.pdf;

import com.kcserver.enumtype.PdfDocumentDensity;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PDFDocumentComposerTest {

    private final PDFLayoutService layoutService =
            new PDFLayoutService();

    private final A4LayoutEngine layoutEngine =
            new A4LayoutEngine(layoutService);

    private final PDFDocumentComposer composer =
            new PDFDocumentComposer(layoutService);

    @Test
    void fuenfA6DokumenteWerdenZuDreiA4SeitenZusammengesetzt()
            throws Exception {

        /*
         * -----------------------------------------------------
         * 1. Fünf künstliche A6-PDFs erzeugen
         * -----------------------------------------------------
         */

        Map<String, byte[]> documents =
                new HashMap<>();

        List<A4LayoutItem> items =
                List.of(
                        a6("Q1"),
                        a6("Q2"),
                        a6("Q3"),
                        a6("Q4"),
                        a6("Q5")
                );

        for (A4LayoutItem item : items) {

            documents.put(
                    item.id(),
                    createTestPdf(
                            item.id()
                    )
            );
        }

        /*
         * -----------------------------------------------------
         * 2. Layout berechnen
         * -----------------------------------------------------
         */

        List<A4LayoutPlacement> placements =
                layoutEngine.layout(items);

        assertEquals(
                5,
                placements.size()
        );

        int pageCount =
                placements.stream()
                        .mapToInt(
                                A4LayoutPlacement::pageNumber
                        )
                        .max()
                        .orElse(0);

        assertEquals(
                3,
                pageCount
        );

        /*
         * -----------------------------------------------------
         * 3. PDF zusammensetzen
         * -----------------------------------------------------
         */

        byte[] result =
                composer.compose(
                        documents,
                        placements
                );

        assertNotNull(result);

        assertTrue(
                result.length > 0
        );

        /*
         * -----------------------------------------------------
         * 4. Ergebnis wieder mit PDFBox öffnen
         * -----------------------------------------------------
         */

        try (
                PDDocument document =
                        org.apache.pdfbox.Loader.loadPDF(
                                result
                        )
        ) {

            assertEquals(
                    3,
                    document.getNumberOfPages()
            );

            /*
             * Beide Seiten müssen DIN A4 sein.
             */
            for (PDPage page :
                    document.getPages()) {

                PDRectangle size =
                        page.getMediaBox();

                assertEquals(
                        PDFLayoutService.A4_WIDTH,
                        size.getWidth(),
                        0.01f
                );

                assertEquals(
                        PDFLayoutService.A4_HEIGHT,
                        size.getHeight(),
                        0.01f
                );
            }
        }
    }

    private byte[] createTestPdf(
            String text
    ) throws Exception {

        return createTestPdf(
                text,
                PDFLayoutService.A6_WIDTH,
                PDFLayoutService.A6_HEIGHT
        );
    }

    /*
     * ---------------------------------------------------------
     * TEST-PDF
     * ---------------------------------------------------------
     */

    private byte[] createTestPdf(
            String text,
            float width,
            float height
    ) throws Exception {

        try (
                PDDocument document =
                        new PDDocument()
        ) {

            PDPage page =
                    new PDPage(
                            new PDRectangle(
                                    width,
                                    height
                            )
                    );

            document.addPage(page);

            try (
                    PDPageContentStream content =
                            new PDPageContentStream(
                                    document,
                                    page
                            )
            ) {

                PDType1Font font =
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA_BOLD
                        );

                content.beginText();

                content.setFont(
                        font,
                        18
                );

                content.newLineAtOffset(
                        20,
                        height / 2
                );

                content.showText(
                        text
                );

                content.endText();
            }

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            document.save(output);

            return output.toByteArray();
        }
    }

    private A4LayoutItem a6(
            String id
    ) {

        return item(
                id,
                PDFLayoutService.A6_WIDTH,
                PDFLayoutService.A6_HEIGHT
        );
    }
    @Test
    void gedrehtesDokumentWirdKorrektZusammengesetzt()
            throws Exception {

        String id = "ROTATED";

        /*
         * Dokument ist absichtlich zu breit für die A4-Nutzfläche.
         * Gedreht passt es hinein.
         *
         * 245 mm × 170 mm
         *
         * normal:
         *   ca. 694 × 482 pt  -> zu breit
         *
         * gedreht:
         *   ca. 482 × 694 pt  -> passt
         */
        float width =
                245f * 72f / 25.4f;

        float height =
                170f * 72f / 25.4f;

        Map<String, byte[]> documents =
                Map.of(
                        id,
                        createTestPdf(
                                id,
                                width,
                                height
                        )
                );

        List<A4LayoutItem> items =
                List.of(
                        item(
                                id,
                                width,
                                height
                        )
                );

        List<A4LayoutPlacement> placements =
                layoutEngine.layout(items);

        assertEquals(
                1,
                placements.size()
        );

        A4LayoutPlacement placement =
                placements.getFirst();

        assertEquals(
                90,
                placement.rotation()
        );

        byte[] result =
                composer.compose(
                        documents,
                        placements
                );

        assertTrue(
                result.length > 0
        );

        try (
                PDDocument document =
                        org.apache.pdfbox.Loader.loadPDF(
                                result
                        )
        ) {

            assertEquals(
                    1,
                    document.getNumberOfPages()
            );
        }
    }

    @Test
    void gemischteDokumenteWerdenAlsA4DokumentZusammengesetzt()
            throws Exception {

        String a4Id = "A4";
        String a5Id = "A5";
        String q1Id = "Q1";
        String q2Id = "Q2";
        String rotatedId = "ROTATED";

        /*
         * ---------------------------------------------------------
         * Testdokumente
         * ---------------------------------------------------------
         */

        float a4Width =
                PDFLayoutService.A4_WIDTH;

        float a4Height =
                PDFLayoutService.A4_HEIGHT;

        float a5Width =
                PDFLayoutService.A5_WIDTH;

        float a5Height =
                PDFLayoutService.A5_HEIGHT;

        float a6Width =
                PDFLayoutService.A6_WIDTH;

        float a6Height =
                PDFLayoutService.A6_HEIGHT;

        float rotatedWidth =
                245f * 72f / 25.4f;

        float rotatedHeight =
                170f * 72f / 25.4f;

        /*
         * ---------------------------------------------------------
         * PDFs erzeugen
         * ---------------------------------------------------------
         */

        Map<String, byte[]> documents =
                Map.of(
                        a4Id,
                        createTestPdf(
                                a4Id,
                                a4Width,
                                a4Height
                        ),

                        a5Id,
                        createTestPdf(
                                a5Id,
                                a5Width,
                                a5Height
                        ),

                        q1Id,
                        createTestPdf(
                                q1Id,
                                a6Width,
                                a6Height
                        ),

                        q2Id,
                        createTestPdf(
                                q2Id,
                                a6Width,
                                a6Height
                        ),

                        rotatedId,
                        createTestPdf(
                                rotatedId,
                                rotatedWidth,
                                rotatedHeight
                        )
                );

        /*
         * ---------------------------------------------------------
         * Layout planen
         * ---------------------------------------------------------
         */

        List<A4LayoutItem> items =
                List.of(
                        item(
                                a4Id,
                                a4Width,
                                a4Height
                        ),

                        item(
                                a5Id,
                                a5Width,
                                a5Height
                        ),

                        item(
                                q1Id,
                                a6Width,
                                a6Height
                        ),

                        item(
                                q2Id,
                                a6Width,
                                a6Height
                        ),

                        item(
                                rotatedId,
                                rotatedWidth,
                                rotatedHeight
                        )
                );

        List<A4LayoutPlacement> placements =
                layoutEngine.layout(items);

        /*
         * ---------------------------------------------------------
         * Grundprüfung Layout
         * ---------------------------------------------------------
         */

        assertEquals(
                items.size(),
                placements.size()
        );

        assertTrue(
                placements.stream()
                        .allMatch(
                                p ->
                                        p.pageNumber() >= 1
                        )
        );

        /*
         * Das ROTATED-Dokument muss tatsächlich
         * gedreht worden sein.
         */
        A4LayoutPlacement rotated =
                placements.stream()
                        .filter(
                                p ->
                                        rotatedId.equals(
                                                p.itemId()
                                        )
                        )
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                90,
                rotated.rotation()
        );

        /*
         * ---------------------------------------------------------
         * PDF zusammensetzen
         * ---------------------------------------------------------
         */

        byte[] result =
                composer.compose(
                        documents,
                        placements
                );

        assertNotNull(result);

        assertTrue(
                result.length > 0
        );

        /*
         * ---------------------------------------------------------
         * Ergebnis-PDF prüfen
         * ---------------------------------------------------------
         */

        try (
                PDDocument document =
                        org.apache.pdfbox.Loader.loadPDF(
                                result
                        )
        ) {

            assertTrue(
                    document.getNumberOfPages() >= 1
            );

            /*
             * Jede Seite muss tatsächlich A4 sein.
             */
            for (PDPage page :
                    document.getPages()) {

                PDRectangle mediaBox =
                        page.getMediaBox();

                assertEquals(
                        PDFLayoutService.A4_WIDTH,
                        mediaBox.getWidth(),
                        0.1f
                );

                assertEquals(
                        PDFLayoutService.A4_HEIGHT,
                        mediaBox.getHeight(),
                        0.1f
                );
            }
        }
    }

    private A4LayoutItem item(
            String id,
            float width,
            float height
    ) {
        return new A4LayoutItem(
                id,
                width,
                height,
                PdfDocumentDensity.MEDIUM,
                true
        );
    }

}