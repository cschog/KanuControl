package com.kcserver.service.pdf;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Plant Dokumente auf DIN-A4-Seiten.
 *
 * Die Engine kennt ausschließlich Dokumentgrößen.
 * Sie rendert keine Dokumente.
 *
 * Grundregeln:
 *
 * - A4-Dokumente werden auf eine eigene Seite skaliert.
 * - A5-Dokumente werden auf einer eigenen Seite platziert,
 *   wenn zwei davon nicht sinnvoll auf eine Seite passen.
 * - A6-Dokumente werden als Raster gepackt.
 * - Kleine Dokumente dürfen bis maximal 70 % verkleinert werden.
 * - Dokumente werden niemals vergrößert.
 * - Rotation um 90° ist möglich.
 * - Die ursprüngliche Dokumentreihenfolge bleibt erhalten.
 */
@Service
@RequiredArgsConstructor
public class A4LayoutEngine {

    private final PDFLayoutService layoutService;

    /**
     * Abstand zwischen Dokumenten.
     */
    private static final float GAP = 6f;

    /**
     * Niemals größer als Originalgröße.
     */
    private static final float MAX_SCALE = 1.0f;

    /**
     * Unter 70 % wird ein Dokument nicht skaliert.
     */
    private static final float MIN_SCALE = 0.70f;

    /**
     * Dokumente bis einschließlich A6 werden als
     * kleine Dokumente behandelt.
     */
    private static final float SMALL_DOCUMENT_AREA =
            PDFLayoutService.A6_WIDTH
                    * PDFLayoutService.A6_HEIGHT
                    * 1.10f;

    /*
     * ---------------------------------------------------------
     * PUBLIC API
     * ---------------------------------------------------------
     */

    public List<A4LayoutPlacement> layout(
            List<A4LayoutItem> items
    ) {

        if (items == null || items.isEmpty()) {
            return List.of();
        }

        List<A4LayoutPlacement> result =
                new ArrayList<>();

        int pageNumber = 1;

        /*
         * Wir arbeiten absichtlich zunächst nach Größe.
         *
         * Große Dokumente blockieren dadurch nicht die Fläche,
         * die wir für viele kleine Quittungen benötigen.
         */
        List<A4LayoutItem> sorted =
                items.stream()
                        .sorted(
                                Comparator.comparingDouble(
                                        this::area
                                ).reversed()
                        )
                        .toList();

        List<A4LayoutItem> smallDocuments =
                new ArrayList<>();

        for (A4LayoutItem item : sorted) {

            if (isSmallDocument(item)) {

                smallDocuments.add(item);

                continue;
            }

            /*
             * Große Dokumente bekommen zunächst eine eigene
             * A4-Seite.
             */
            A4LayoutPlacement placement =
                    placeLargeDocument(
                            item,
                            pageNumber
                    );

            result.add(placement);

            pageNumber++;
        }

        /*
         * Kleine Dokumente werden anschließend möglichst
         * kompakt als Raster gepackt.
         */
        if (!smallDocuments.isEmpty()) {

            List<A4LayoutPlacement> smallPlacements =
                    placeSmallDocuments(
                            smallDocuments,
                            pageNumber
                    );

            result.addAll(smallPlacements);
        }

        /*
         * Am Ende wieder die ursprüngliche Dokumentreihenfolge
         * herstellen.
         */
        return restoreItemOrder(
                items,
                result
        );
    }

    /**
     * Breite der für Dokumente verfügbaren Fläche.
     */
    public float getPackingWidth() {

        return layoutService.getContentWidth();
    }

    /**
     * Höhe der für Dokumente verfügbaren Fläche.
     */
    public float getPackingHeight() {

        return layoutService.getContentHeight();
    }

    /*
     * ---------------------------------------------------------
     * GROSSE DOKUMENTE
     * ---------------------------------------------------------
     */

    private A4LayoutPlacement placeLargeDocument(
            A4LayoutItem item,
            int pageNumber
    ) {

        float contentWidth =
                layoutService.getContentWidth();

        float contentHeight =
                layoutService.getContentHeight();

        /*
         * Normal.
         */
        Candidate normal =
                createCandidate(
                        item,
                        pageNumber,
                        contentWidth,
                        contentHeight,
                        false
                );

        /*
         * Gedreht.
         */
        Candidate rotated =
                createCandidate(
                        item,
                        pageNumber,
                        contentWidth,
                        contentHeight,
                        true
                );

        Candidate selected =
                selectBestCandidate(
                        normal,
                        rotated
                );

        if (selected != null) {

            return selected.placement();
        }

        /*
         * Sollte ein Dokument selbst bei 70 % nicht
         * auf die Seite passen, wird es auf mehrere Seiten
         * verteilt.
         */
        return splitItem(
                item,
                pageNumber
        ).getFirst();
    }

    private Candidate createCandidate(
            A4LayoutItem item,
            int pageNumber,
            float availableWidth,
            float availableHeight,
            boolean rotated
    ) {

        float sourceWidth =
                rotated
                        ? item.height()
                        : item.width();

        float sourceHeight =
                rotated
                        ? item.width()
                        : item.height();

        float scaleX =
                availableWidth / sourceWidth;

        float scaleY =
                availableHeight / sourceHeight;

        float scale =
                Math.min(
                        scaleX,
                        scaleY
                );

        scale =
                Math.min(
                        scale,
                        MAX_SCALE
                );

        /*
         * Wenn das Dokument nur unter 70 %
         * passen würde, ist diese Variante ungültig.
         */
        if (scale < MIN_SCALE) {
            return null;
        }

        float width =
                sourceWidth * scale;

        float height =
                sourceHeight * scale;

        float x =
                layoutService.getMarginLeft();

        float y =
                layoutService.getMarginBottom();

        A4LayoutPlacement placement =
                new A4LayoutPlacement(
                        item.id(),
                        pageNumber,
                        x,
                        y,
                        width,
                        height,
                        0,
                        item.height(),
                        rotated ? 90 : 0,
                        false
                );

        return new Candidate(
                placement,
                scale
        );
    }

    private Candidate selectBestCandidate(
            Candidate normal,
            Candidate rotated
    ) {

        if (normal == null) {
            return rotated;
        }

        if (rotated == null) {
            return normal;
        }

        /*
         * Möglichst große Darstellung bevorzugen.
         */
        return rotated.scale() > normal.scale()
                ? rotated
                : normal;
    }

    /*
     * ---------------------------------------------------------
     * KLEINE DOKUMENTE
     * ---------------------------------------------------------
     */

    private List<A4LayoutPlacement> placeSmallDocuments(
            List<A4LayoutItem> items,
            int startPage
    ) {

        List<A4LayoutPlacement> result =
                new ArrayList<>();

        float availableWidth =
                layoutService.getContentWidth();

        float availableHeight =
                layoutService.getContentHeight();

        /*
         * Wir arbeiten mit maximal vier Dokumenten
         * pro Seite.
         *
         * Das ist insbesondere für A6-Quittungen
         * das gewünschte 2 × 2 Layout.
         */
        int index = 0;

        int page =
                startPage;

        while (index < items.size()) {

            int remaining =
                    items.size() - index;

            int count =
                    Math.min(
                            4,
                            remaining
                    );

            List<A4LayoutItem> pageItems =
                    items.subList(
                            index,
                            index + count
                    );

            result.addAll(
                    placeSmallPage(
                            pageItems,
                            page
                    )
            );

            index += count;
            page++;
        }

        return result;
    }

    /**
     * Platziert bis zu vier kleine Dokumente.
     *
     * Für vier Dokumente:
     *
     *   +-------+-------+
     *   |   1   |   2   |
     *   +-------+-------+
     *   |   3   |   4   |
     *   +-------+-------+
     */
    private List<A4LayoutPlacement> placeSmallPage(
            List<A4LayoutItem> items,
            int pageNumber
    ) {

        List<A4LayoutPlacement> result =
                new ArrayList<>();

        float contentWidth =
                layoutService.getContentWidth();

        float contentHeight =
                layoutService.getContentHeight();

        int count =
                items.size();

        int columns;

        int rows;

        if (count <= 1) {

            columns = 1;
            rows = 1;

        } else if (count == 2) {

            columns = 1;
            rows = 2;

        } else {

            columns = 2;
            rows = 2;
        }

        float cellWidth =
                (
                        contentWidth
                                - GAP * (columns - 1)
                )
                        / columns;

        float cellHeight =
                (
                        contentHeight
                                - GAP * (rows - 1)
                )
                        / rows;

        for (int i = 0; i < count; i++) {

            A4LayoutItem item =
                    items.get(i);

            int column =
                    i % columns;

            int row =
                    i / columns;

            float cellX =
                    layoutService.getMarginLeft()
                            + column
                            * (cellWidth + GAP);

            /*
             * PDF-Koordinaten laufen von unten nach oben.
             */
            float cellY =
                    layoutService.getMarginBottom()
                            + (
                            rows
                                    - 1
                                    - row
                    )
                            * (cellHeight + GAP);

            Candidate normal =
                    createCandidateInCell(
                            item,
                            pageNumber,
                            cellX,
                            cellY,
                            cellWidth,
                            cellHeight,
                            false
                    );

            Candidate rotated =
                    createCandidateInCell(
                            item,
                            pageNumber,
                            cellX,
                            cellY,
                            cellWidth,
                            cellHeight,
                            true
                    );

            Candidate selected =
                    selectBestCandidate(
                            normal,
                            rotated
                    );

            if (selected != null) {

                result.add(
                        selected.placement()
                );

            } else {

                /*
                 * Ein Dokument passt nicht in die Zelle.
                 *
                 * Das sollte bei normalen A6-Quittungen
                 * nicht vorkommen. Zur Sicherheit wird es
                 * auf einer eigenen Seite platziert.
                 */
                result.add(
                        placeLargeDocument(
                                item,
                                pageNumber
                        )
                );
            }
        }

        return result;
    }

    private Candidate createCandidateInCell(
            A4LayoutItem item,
            int pageNumber,
            float x,
            float y,
            float cellWidth,
            float cellHeight,
            boolean rotated
    ) {

        float sourceWidth =
                rotated
                        ? item.height()
                        : item.width();

        float sourceHeight =
                rotated
                        ? item.width()
                        : item.height();

        float scaleX =
                cellWidth / sourceWidth;

        float scaleY =
                cellHeight / sourceHeight;

        float scale =
                Math.min(
                        scaleX,
                        scaleY
                );

        scale =
                Math.min(
                        scale,
                        MAX_SCALE
                );

        if (scale < MIN_SCALE) {
            return null;
        }

        float width =
                sourceWidth * scale;

        float height =
                sourceHeight * scale;

        /*
         * Innerhalb der Zelle zentrieren.
         */
        float outputX =
                x
                        + (cellWidth - width) / 2f;

        float outputY =
                y
                        + (cellHeight - height) / 2f;

        A4LayoutPlacement placement =
                new A4LayoutPlacement(
                        item.id(),
                        pageNumber,
                        outputX,
                        outputY,
                        width,
                        height,
                        0,
                        item.height(),
                        rotated ? 90 : 0,
                        false
                );

        return new Candidate(
                placement,
                scale
        );
    }

    /*
     * ---------------------------------------------------------
     * SPLIT
     * ---------------------------------------------------------
     */

    private List<A4LayoutPlacement> splitItem(
            A4LayoutItem item,
            int startPage
    ) {

        List<A4LayoutPlacement> result =
                new ArrayList<>();

        float availableWidth =
                layoutService.getContentWidth();

        float availableHeight =
                layoutService.getContentHeight();

        float scale =
                Math.min(
                        MAX_SCALE,
                        availableWidth / item.width()
                );

        float outputWidth =
                item.width() * scale;

        float sourceHeightPerPage =
                availableHeight / scale;

        float remainingHeight =
                item.height();

        float sourceY = 0;

        int page =
                startPage;

        boolean first = true;

        while (remainingHeight > 0.01f) {

            float sourceHeight =
                    Math.min(
                            sourceHeightPerPage,
                            remainingHeight
                    );

            float outputHeight =
                    sourceHeight * scale;

            result.add(
                    new A4LayoutPlacement(
                            item.id(),
                            page,
                            layoutService.getMarginLeft(),
                            layoutService.getMarginBottom(),
                            outputWidth,
                            outputHeight,
                            sourceY,
                            sourceHeight,
                            0,
                            !first
                    )
            );

            remainingHeight -= sourceHeight;
            sourceY += sourceHeight;

            page++;
            first = false;
        }

        return result;
    }

    /*
     * ---------------------------------------------------------
     * HILFSMETHODEN
     * ---------------------------------------------------------
     */

    private boolean isSmallDocument(
            A4LayoutItem item
    ) {

        return area(item)
                <= SMALL_DOCUMENT_AREA;
    }

    private double area(
            A4LayoutItem item
    ) {

        return item.width()
                * item.height();
    }

    private List<A4LayoutPlacement> restoreItemOrder(
            List<A4LayoutItem> items,
            List<A4LayoutPlacement> placements
    ) {

        List<String> order =
                items.stream()
                        .map(A4LayoutItem::id)
                        .toList();

        return placements.stream()
                .sorted(
                        Comparator
                                .comparingInt(
                                        (A4LayoutPlacement p) ->
                                                order.indexOf(
                                                        p.itemId()
                                                )
                                )
                                .thenComparingInt(
                                        A4LayoutPlacement::pageNumber
                                )
                )
                .toList();
    }

    /*
     * ---------------------------------------------------------
     * INTERN
     * ---------------------------------------------------------
     */

    private record Candidate(
            A4LayoutPlacement placement,
            float scale
    ) {
    }
}