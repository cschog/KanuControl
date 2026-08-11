package com.kcserver.service.pdf;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class A4LayoutEngineTest {

    private final A4LayoutEngine engine =
            new A4LayoutEngine(
                    new PDFLayoutService()
            );

    private final PDFLayoutService layoutService =
            new PDFLayoutService();


    @Test
    void vierA6DokumenteWerdenSinnvollVerteilt() {

        List<A4LayoutItem> items = List.of(
                a6("Q1"),
                a6("Q2"),
                a6("Q3"),
                a6("Q4")
        );

        List<A4LayoutPlacement> result =
                engine.layout(items);

        result.forEach(p ->
                System.out.printf(
                        "%s: Seite %d x=%.1f y=%.1f w=%.1f h=%.1f rotation=%d%n",
                        p.itemId(),
                        p.pageNumber(),
                        p.x(),
                        p.y(),
                        p.width(),
                        p.height(),
                        p.rotation()
                )
        );

        assertEquals(
                4,
                result.size()
        );

        assertTrue(
                result.stream()
                        .allMatch(this::liegtInnerhalbDerNutzflaeche)
        );

        assertTrue(
                result.stream()
                        .allMatch(
                                p -> p.width()
                                        >= PDFLayoutService.A6_WIDTH * 0.70f
                        )
        );
    }

    @Test
    void zweiA5DokumenteBrauchenZweiSeiten() {

        List<A4LayoutItem> items = List.of(
                new A4LayoutItem(
                        "A5-1",
                        PDFLayoutService.A5_WIDTH,
                        PDFLayoutService.A5_HEIGHT
                ),
                new A4LayoutItem(
                        "A5-2",
                        PDFLayoutService.A5_WIDTH,
                        PDFLayoutService.A5_HEIGHT
                )
        );

        List<A4LayoutPlacement> result =
                engine.layout(items);

        result.forEach(p ->
                System.out.printf(
                        "%s: Seite %d x=%.1f y=%.1f w=%.1f h=%.1f rotation=%d%n",
                        p.itemId(),
                        p.pageNumber(),
                        p.x(),
                        p.y(),
                        p.width(),
                        p.height(),
                        p.rotation()
                )
        );

        assertEquals(2, result.size());

        assertEquals(
                2,
                result.stream()
                        .mapToInt(A4LayoutPlacement::pageNumber)
                        .max()
                        .orElse(0)
        );
    }

    @Test
    void A4DokumentBrauchtEineEigeneSeite() {

        List<A4LayoutItem> items = List.of(
                new A4LayoutItem(
                        "A4",
                        PDFLayoutService.A4_WIDTH,
                        PDFLayoutService.A4_HEIGHT
                )
        );

        List<A4LayoutPlacement> result =
                engine.layout(items);

        assertEquals(1, result.size());

        A4LayoutPlacement placement =
                result.getFirst();

        assertEquals(
                1,
                placement.pageNumber()
        );

        assertTrue(
                placement.width()
                        <= engine.getPackingWidth()
        );

        assertTrue(
                placement.height()
                        <= engine.getPackingHeight()
        );
    }

    @Test
    void fuenfA6DokumenteBrauchenZweiSeiten() {

        List<A4LayoutItem> items = List.of(
                a6("Q1"),
                a6("Q2"),
                a6("Q3"),
                a6("Q4"),
                a6("Q5")
        );

        List<A4LayoutPlacement> result =
                engine.layout(items);

        result.forEach(p ->
                System.out.printf(
                        "%s: Seite %d x=%.1f y=%.1f w=%.1f h=%.1f rotation=%d%n",
                        p.itemId(),
                        p.pageNumber(),
                        p.x(),
                        p.y(),
                        p.width(),
                        p.height(),
                        p.rotation()
                )
        );

        assertEquals(5, result.size());

        int maxPage =
                result.stream()
                        .mapToInt(
                                A4LayoutPlacement::pageNumber
                        )
                        .max()
                        .orElse(0);

        assertEquals(
                5,
                result.size()
        );

        assertEquals(
                2,
                maxPage
        );

        assertTrue(
                result.stream()
                        .allMatch(
                                p ->
                                        p.x() >= PDFLayoutService.MARGIN_LEFT
                                                && p.y() >= PDFLayoutService.MARGIN_BOTTOM
                                                && p.x() + p.width()
                                                <= PDFLayoutService.PAGE_WIDTH
                                                - PDFLayoutService.MARGIN_RIGHT
                                                + 0.01f
                                                && p.y() + p.height()
                                                <= PDFLayoutService.PAGE_HEIGHT
                                                - PDFLayoutService.MARGIN_TOP
                                                + 0.01f
                        )
        );
    }

    private A4LayoutItem a6(String id) {

        return new A4LayoutItem(
                id,
                PDFLayoutService.A6_WIDTH,
                PDFLayoutService.A6_HEIGHT
        );
    }

    @Test
    void urspruenglicheDokumentreihenfolgeBleibtErhalten() {

        List<A4LayoutItem> items = List.of(
                new A4LayoutItem(
                        "Q1",
                        PDFLayoutService.A6_WIDTH,
                        PDFLayoutService.A6_HEIGHT
                ),
                new A4LayoutItem(
                        "A4",
                        PDFLayoutService.A4_WIDTH,
                        PDFLayoutService.A4_HEIGHT
                ),
                new A4LayoutItem(
                        "Q2",
                        PDFLayoutService.A6_WIDTH,
                        PDFLayoutService.A6_HEIGHT
                )
        );

        List<A4LayoutPlacement> result =
                engine.layout(items);

        assertEquals(
                "Q1",
                result.get(0).itemId()
        );

        assertEquals(
                "A4",
                result.get(1).itemId()
        );

        assertEquals(
                "Q2",
                result.get(2).itemId()
        );
    }
    @Test
    void gemischteDokumenteWerdenAufA4Verteilt() {

        List<A4LayoutItem> items = List.of(

                new A4LayoutItem(
                        "KONTOAUSZUG",
                        PDFLayoutService.A4_WIDTH,
                        PDFLayoutService.A4_HEIGHT
                ),

                a6("Q1"),
                a6("Q2"),
                a6("Q3"),

                new A4LayoutItem(
                        "KASSENZETTEL",
                        mm(100),
                        mm(300)
                ),

                a6("Q4"),
                a6("Q5")
        );

        List<A4LayoutPlacement> result =
                engine.layout(items);


        assertEquals(
                7,
                result.size()
        );

        /*
         * Jedes Dokument muss vollständig innerhalb
         * der A4-Nutzfläche liegen.
         */
        assertTrue(
                result.stream()
                        .allMatch(this::liegtInnerhalbDerNutzflaeche)
        );

        result.forEach(p ->
                System.out.printf(
                        "%s: Seite %d x=%.1f y=%.1f w=%.1f h=%.1f%n",
                        p.itemId(),
                        p.pageNumber(),
                        p.x(),
                        p.y(),
                        p.width(),
                        p.height()
                )
        );
    }

    private float mm(float mm) {
        return mm * 72f / 25.4f;
    }

    private boolean liegtInnerhalbDerNutzflaeche(
            A4LayoutPlacement p
    ) {

        return p.x()
                >= PDFLayoutService.MARGIN_LEFT

                && p.y()
                >= PDFLayoutService.MARGIN_BOTTOM

                && p.x() + p.width()
                <= PDFLayoutService.PAGE_WIDTH
                - PDFLayoutService.MARGIN_RIGHT
                + 0.01f

                && p.y() + p.height()
                <= PDFLayoutService.PAGE_HEIGHT
                - PDFLayoutService.MARGIN_TOP
                + 0.01f;
    }

    @Test
    void vierA6WerdenAls2x2AnordnungPlatziert() {

        List<A4LayoutItem> items = List.of(
                a6("Q1"),
                a6("Q2"),
                a6("Q3"),
                a6("Q4")
        );

        List<A4LayoutPlacement> result =
                engine.layout(items);

        assertEquals(4, result.size());

        assertEquals(
                1,
                result.stream()
                        .mapToInt(A4LayoutPlacement::pageNumber)
                        .max()
                        .orElse(0)
        );

        result.forEach(p ->
                System.out.printf(
                        "%s: x=%.1f y=%.1f w=%.1f h=%.1f rot=%d%n",
                        p.itemId(),
                        p.x(),
                        p.y(),
                        p.width(),
                        p.height(),
                        p.rotation()
                )
        );
    }
}