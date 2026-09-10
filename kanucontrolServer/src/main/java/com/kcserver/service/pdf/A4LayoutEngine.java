package com.kcserver.service.pdf;

import com.kcserver.enumtype.ReferenzObjekt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Plant Dokumente auf DIN-A4-Seiten.
 *
 * Die Engine kennt:
 *
 * - Dokumentgröße über ReferenzObjekt
 * - tatsächliche PDF-Abmessungen
 * - Density zur Bestimmung der Mindestskalierung
 *
 * Layoutregeln:
 *
 * - A4-Dokumente erhalten grundsätzlich eine eigene A4-Seite.
 * - A5-Dokumente werden bevorzugt quer platziert.
 * - A5 hochkant wird nur alleine auf einer Seite verwendet.
 * - A5 + 2 x A6 werden bevorzugt gemeinsam platziert.
 * - 4 x A6 werden bevorzugt im 2 x 2 Raster platziert.
 * - 8 x A7 werden bevorzugt im 2 x 4 Raster platziert.
 * - 2 x A6 können gemeinsam auf einer Seite platziert werden.
 * - Die Ausgabeorientierung wird abhängig vom Layout
 *   festgelegt; quer erfasste Dokumente können dafür
 *   technisch um 90° gedreht werden.
 * - Dokumente werden niemals größer als ihre Originalgröße.
 * - Dokumente dürfen höchstens auf 80 % verkleinert werden.
 * - Die ursprüngliche Dokumentreihenfolge bleibt erhalten.
 */
@Service
@RequiredArgsConstructor
public class A4LayoutEngine {

    private record PagePlan(
            List<A4LayoutPlacement> placements,
            int itemCount,
            float minimumScale
    ) {
    }

    private final PDFLayoutService layoutService;

    /**
     * Abstand zwischen Dokumenten.
     */
    private static final float GAP = 6f;

    /**
     * Dokumente dürfen höchstens auf 80 % ihrer
     * Originalgröße verkleinert werden.
     */
    private static final float MIN_SCALE = 0.80f;

    /**
     * Dokumente dürfen niemals vergrößert werden.
     */
    private static final float MAX_SCALE = 1.0f;

    /*
     * =========================================================
     * LAYOUT-TEMPLATES
     * =========================================================
     */

    private enum LayoutOrientation {
        PORTRAIT,
        LANDSCAPE
    }

    private record LayoutSlot(
            float x,
            float y,
            float width,
            float height,
            LayoutOrientation orientation
    ) {
    }

    private record LayoutCandidate(
            List<A4LayoutPlacement> placements,
            float minimumScale
    ) {
    }


    /*
     * =========================================================
     * PUBLIC API
     * =========================================================
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
        int index = 0;

        while (index < items.size()) {

            A4LayoutItem current =
                    items.get(index);

            /*
             * A4 bleibt immer alleine.
             */
            if (isA4(current)) {

                result.addAll(
                        placeSinglePortraitDocument(
                                current,
                                pageNumber
                        )
                );

                pageNumber =
                        nextPageNumber(
                                result,
                                pageNumber
                        );

                index++;

                continue;
            }


            /*
             * =====================================================
             * LOOKAHEAD
             * =====================================================
             *
             * Es werden maximal die nächsten acht Dokumente
             * betrachtet.
             *
             * Die Seite darf dabei niemals über ein A4-Dokument
             * hinaus geplant werden.
             */
            List<A4LayoutItem> window =
                    getPlanningWindow(
                            items,
                            index
                    );


            PagePlan bestPlan =
                    findBestPagePlan(
                            window,
                            pageNumber
                    );


            if (bestPlan != null) {

                result.addAll(
                        bestPlan.placements()
                );

                pageNumber =
                        nextPageNumber(
                                result,
                                pageNumber
                        );

                index +=
                        bestPlan.itemCount();

                continue;
            }


            /*
             * =====================================================
             * FALLBACK
             * =====================================================
             */

            result.addAll(
                    placeSingleDocument(
                            current,
                            pageNumber
                    )
            );

            pageNumber =
                    nextPageNumber(
                            result,
                            pageNumber
                    );

            index++;
        }

        return result;
    }

    private PagePlan findBestPagePlan(
            List<A4LayoutItem> window,
            int pageNumber
    ) {

        if (window == null || window.isEmpty()) {
            return null;
        }

        PagePlan best = null;

        /*
         * Wir prüfen alle aufeinanderfolgenden Prefixe
         * des Fensters.
         *
         * Bei acht Dokumenten also:
         *
         * 1
         * 1-2
         * 1-3
         * ...
         * 1-8
         *
         * Die Dokumentreihenfolge bleibt dadurch erhalten.
         */
        for (int count = 1;
             count <= window.size();
             count++) {

            List<A4LayoutItem> items =
                    window.subList(
                            0,
                            count
                    );

            best =
                    selectBetterPlan(
                            best,
                            createPagePlan(
                                    items,
                                    pageNumber
                            )
                    );
        }

        return best;
    }

    private PagePlan createPagePlan(
            List<A4LayoutItem> items,
            List<A4LayoutPlacement> placements
    ) {

        if (placements == null
                || placements.isEmpty()) {

            return null;
        }

        float minimumScale =
                MAX_SCALE;

        for (A4LayoutPlacement placement :
                placements) {

            A4LayoutItem item =
                    findItem(
                            items,
                            placement.itemId()
                    );

            if (item == null) {
                return null;
            }

            minimumScale =
                    Math.min(
                            minimumScale,
                            getPlacementScale(
                                    item,
                                    placement
                            )
                    );
        }

        return new PagePlan(
                placements,
                items.size(),
                minimumScale
        );
    }

    private PagePlan createPagePlan(
            List<A4LayoutItem> items,
            int pageNumber
    ) {

        int count =
                items.size();


        /*
         * =========================================================
         * 8 x A7
         * =========================================================
         */
        if (count == 8
                && allA7(items)) {

            List<A4LayoutPlacement> placements =
                    placeEightA7(
                            items.get(0),
                            items.get(1),
                            items.get(2),
                            items.get(3),
                            items.get(4),
                            items.get(5),
                            items.get(6),
                            items.get(7),
                            pageNumber
                    );

            return createPagePlan(
                    items,
                    placements
            );
        }


        /*
         * =========================================================
         * A7-BLOCK
         *
         * 1 bis 8 A7.
         * =========================================================
         */
        if (count >= 1
                && count <= 8
                && allA7(items)) {

            List<A4LayoutPlacement> placements =
                    placeA7Block(
                            items,
                            pageNumber
                    );

            return createPagePlan(
                    items,
                    placements
            );
        }


        /*
         * =========================================================
         * A5 + 4 x A7
         * =========================================================
         */
        if (count == 5
                && countFormat(
                items,
                ReferenzObjekt.DIN_A5
        ) == 1
                && countFormat(
                items,
                ReferenzObjekt.DIN_A7
        ) == 4) {

            return createFourA7PlusA5Plan(
                    items,
                    pageNumber
            );
        }

        /*
         * =========================================================
         * 2 x A6 + 4 x A7
         *
         * Variante aus deiner bisherigen Engine:
         * 4 A7 + 2 A6
         * =========================================================
         */
        if (count == 6
                && countFormat(
                items,
                ReferenzObjekt.DIN_A7
        ) == 4
                && countFormat(
                items,
                ReferenzObjekt.DIN_A6
        ) == 2) {

            return createTwoA6PlusFourA7Plan(
                    items,
                    pageNumber
            );
        }


        /*
         * =========================================================
         * A5 + 2 x A7
         * =========================================================
         */
        if (count == 3
                && countFormat(
                items,
                ReferenzObjekt.DIN_A5
        ) == 1
                && countFormat(
                items,
                ReferenzObjekt.DIN_A7
        ) == 2) {

            List<A4LayoutPlacement> placements =
                    placeA5PlusTwoA7(
                            items.get(0),
                            items.get(1),
                            items.get(2),
                            pageNumber
                    );

            return createPagePlan(
                    items,
                    placements
            );
        }


        /*
         * =========================================================
         * A6 + 2 x A7
         * =========================================================
         */
        if (count == 3
                && countFormat(
                items,
                ReferenzObjekt.DIN_A6
        ) == 1
                && countFormat(
                items,
                ReferenzObjekt.DIN_A7
        ) == 2) {

            List<A4LayoutPlacement> placements =
                    placeA6PlusTwoA7(
                            items.get(0),
                            items.get(1),
                            items.get(2),
                            pageNumber
                    );

            return createPagePlan(
                    items,
                    placements
            );
        }


        /*
         * =========================================================
         * A5 + 2 x A6
         * =========================================================
         */
        if (count == 3
                && countFormat(
                items,
                ReferenzObjekt.DIN_A5
        ) == 1
                && countFormat(
                items,
                ReferenzObjekt.DIN_A6
        ) == 2) {

            List<A4LayoutPlacement> placements =
                    placeA5PlusTwoA6(
                            items.get(0),
                            items.get(1),
                            items.get(2),
                            pageNumber
                    );

            return createPagePlan(
                    items,
                    placements
            );
        }


        /*
         * =========================================================
         * 4 x A6
         * =========================================================
         */
        if (count == 4
                && allA6(items)) {

            List<A4LayoutPlacement> placements =
                    placeFourA6(
                            items.get(0),
                            items.get(1),
                            items.get(2),
                            items.get(3),
                            pageNumber
                    );

            return createPagePlan(
                    items,
                    placements
            );
        }


        /*
         * =========================================================
         * A5 + A6
         * =========================================================
         */
        if (count == 2
                && countFormat(
                items,
                ReferenzObjekt.DIN_A5
        ) == 1
                && countFormat(
                items,
                ReferenzObjekt.DIN_A6
        ) == 1) {

            List<A4LayoutPlacement> placements =
                    placeA5PlusA6(
                            items.get(0),
                            items.get(1),
                            pageNumber
                    );

            return createPagePlan(
                    items,
                    placements
            );
        }


        /*
         * =========================================================
         * 2 x A6
         * =========================================================
         */
        if (count == 2
                && allA6(items)) {

            List<A4LayoutPlacement> placements =
                    placeTwoA6(
                            items.get(0),
                            items.get(1),
                            pageNumber
                    );

            return createPagePlan(
                    items,
                    placements
            );
        }


        /*
         * =========================================================
         * GENERISCHES PAAR
         * =========================================================
         */
        if (count == 2
                && canUseGenericPair(
                items.get(0),
                items.get(1)
        )) {

            List<A4LayoutPlacement> placements =
                    placePair(
                            items.get(0),
                            items.get(1),
                            pageNumber
                    );

            return createPagePlan(
                    items,
                    placements
            );
        }


        /*
         * =========================================================
         * EINZELDOKUMENT
         * =========================================================
         */
        if (count == 1) {

            A4LayoutItem item =
                    items.getFirst();

            List<A4LayoutPlacement> placements;

            if (isA5(item)) {

                placements =
                        placeSingleA5(
                                item,
                                pageNumber
                        );

            } else {

                placements =
                        placeSingleDocument(
                                item,
                                pageNumber
                        );
            }

            return createPagePlan(
                    items,
                    placements
            );
        }


        return null;
    }

    private A4LayoutItem findItem(
            List<A4LayoutItem> items,
            String itemId
    ) {

        for (A4LayoutItem item : items) {

            if (item.id()
                    .equals(itemId)) {

                return item;
            }
        }

        return null;
    }

    private PagePlan selectBetterPlan(
            PagePlan current,
            PagePlan candidate
    ) {

        if (candidate == null) {
            return current;
        }

        if (current == null) {
            return candidate;
        }


        /*
         * =========================================================
         * 1. Möglichst viele Dokumente auf einer Seite
         * =========================================================
         */
        if (candidate.itemCount()
                > current.itemCount()) {

            return candidate;
        }

        if (candidate.itemCount()
                < current.itemCount()) {

            return current;
        }


        /*
         * =========================================================
         * 2. Bei gleicher Dokumentanzahl:
         *    bessere Mindestskalierung gewinnt
         * =========================================================
         */
        if (candidate.minimumScale()
                > current.minimumScale()) {

            return candidate;
        }

        return current;
    }

    private boolean allA6(
            List<A4LayoutItem> items
    ) {

        return items.stream()
                .allMatch(
                        this::isA6
                );
    }

    private boolean allA7(
            List<A4LayoutItem> items
    ) {

        return items.stream()
                .allMatch(
                        this::isA7
                );
    }

    private int countFormat(
            List<A4LayoutItem> items,
            ReferenzObjekt format
    ) {

        return (int) items.stream()
                .filter(
                        item -> item.referenzObjekt()
                                == format
                )
                .count();
    }

    private boolean canUseGenericPair(
            A4LayoutItem first,
            A4LayoutItem second
    ) {

        return !isA4(first)
                && !isA4(second);
    }

    private List<A4LayoutItem> getPlanningWindow(
            List<A4LayoutItem> items,
            int startIndex
    ) {

        List<A4LayoutItem> result =
                new ArrayList<>();

        for (int i = startIndex;
             i < items.size() && result.size() < 8;
             i++) {

            A4LayoutItem item =
                    items.get(i);

            /*
             * Ein A4 beendet das aktuelle Planungsfenster.
             *
             * Es wird auf einer eigenen Seite verarbeitet.
             */
            if (isA4(item)) {
                break;
            }

            result.add(item);
        }

        return result;
    }

    private List<A4LayoutPlacement> placeA5PlusTwoA7(
            A4LayoutItem first,
            A4LayoutItem second,
            A4LayoutItem third,
            int pageNumber
    ) {

        A4LayoutItem a5 =
                isA5(first)
                        ? first
                        : isA5(second)
                        ? second
                        : third;

        List<A4LayoutItem> a7Items =
                new ArrayList<>();

        if (isA7(first)) {
            a7Items.add(first);
        }

        if (isA7(second)) {
            a7Items.add(second);
        }

        if (isA7(third)) {
            a7Items.add(third);
        }

        if (a7Items.size() != 2) {
            return null;
        }

        float contentWidth =
                layoutService.getContentWidth();

        float contentHeight =
                layoutService.getContentHeight();

        float left =
                layoutService.getMarginLeft();

        float bottom =
                layoutService.getMarginBottom();


        /*
         * A5 oben, 2 x A7 unten.
         *
         * Zunächst 50/50 aufteilen.
         */
        float areaHeight =
                (contentHeight - GAP) / 2f;

        float a7CellWidth =
                (contentWidth - GAP) / 2f;


        /*
         * A5 oben – zwingend Landscape.
         */
        Candidate a5Candidate =
                createLandscapeCandidate(
                        a5,
                        pageNumber,
                        left,
                        bottom
                                + areaHeight
                                + GAP,
                        contentWidth,
                        areaHeight
                );


        /*
         * A7 links unten – zwingend Landscape.
         */
        Candidate a7First =
                createLandscapeCandidate(
                        a7Items.get(0),
                        pageNumber,
                        left,
                        bottom,
                        a7CellWidth,
                        areaHeight
                );


        /*
         * A7 rechts unten – zwingend Landscape.
         */
        Candidate a7Second =
                createLandscapeCandidate(
                        a7Items.get(1),
                        pageNumber,
                        left
                                + a7CellWidth
                                + GAP,
                        bottom,
                        a7CellWidth,
                        areaHeight
                );


        if (a5Candidate == null
                || a7First == null
                || a7Second == null) {

            return null;
        }


        /*
         * Ursprüngliche Dokumentreihenfolge erhalten.
         */
        List<A4LayoutPlacement> placements =
                new ArrayList<>(3);

        for (A4LayoutItem item :
                List.of(first, second, third)) {

            if (item == a5) {

                placements.add(
                        a5Candidate.placement()
                );

            } else if (item == a7Items.get(0)) {

                placements.add(
                        a7First.placement()
                );

            } else {

                placements.add(
                        a7Second.placement()
                );
            }
        }

        return placements;
    }


    public float getPackingWidth() {
        return layoutService.getContentWidth();
    }


    public float getPackingHeight() {
        return layoutService.getContentHeight();
    }


    /*
     * =========================================================
     * FORMATPRÜFUNGEN
     * =========================================================
     */

    private boolean isA4(
            A4LayoutItem item
    ) {
        return item.referenzObjekt()
                == ReferenzObjekt.DIN_A4;
    }


    private boolean isA5(
            A4LayoutItem item
    ) {
        return item.referenzObjekt()
                == ReferenzObjekt.DIN_A5;
    }


    private boolean isA6(
            A4LayoutItem item
    ) {
        return item.referenzObjekt()
                == ReferenzObjekt.DIN_A6;
    }


    private boolean isA7(
            A4LayoutItem item
    ) {
        return item.referenzObjekt()
                == ReferenzObjekt.DIN_A7;
    }


    /*
     * =========================================================
     * EINZELNES DOKUMENT
     * =========================================================
     */

    private List<A4LayoutPlacement> placeSingleDocument(
            A4LayoutItem item,
            int pageNumber
    ) {

        float contentWidth =
                layoutService.getContentWidth();

        float contentHeight =
                layoutService.getContentHeight();

        Candidate normal =
                createCandidate(
                        item,
                        pageNumber,
                        contentWidth,
                        contentHeight,
                        false
                );

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

        if (selected == null) {

            return splitItem(
                    item,
                    pageNumber
            );
        }

        return List.of(
                selected.placement()
        );
    }

    /*
     * =========================================================
     * EINZELNES DOKUMENT – PORTRAIT
     * =========================================================
     *
     * Wird für A4 und bei Bedarf für alleine stehendes A5
     * verwendet.
     *
     * Die gewünschte Ausgabe ist ausdrücklich Hochformat.
     */
    private List<A4LayoutPlacement> placeSinglePortraitDocument(
            A4LayoutItem item,
            int pageNumber
    ) {

        float contentWidth =
                layoutService.getContentWidth();

        float contentHeight =
                layoutService.getContentHeight();

        Candidate normal =
                createCandidateInArea(
                        item,
                        pageNumber,
                        layoutService.getMarginLeft(),
                        layoutService.getMarginBottom(),
                        contentWidth,
                        contentHeight,
                        false
                );

        Candidate rotated =
                createCandidateInArea(
                        item,
                        pageNumber,
                        layoutService.getMarginLeft(),
                        layoutService.getMarginBottom(),
                        contentWidth,
                        contentHeight,
                        true
                );

        Candidate selected = null;

        if (normal != null
                && normal.placement().height()
                >= normal.placement().width()) {

            selected = normal;
        }

        if (rotated != null
                && rotated.placement().height()
                >= rotated.placement().width()) {

            if (selected == null
                    || rotated.scale() > selected.scale()) {

                selected = rotated;
            }
        }

        if (selected == null) {

            return splitItem(
                    item,
                    pageNumber
            );
        }

        return List.of(
                selected.placement()
        );
    }
    /*
     * =========================================================
     * A5 ALLEINE
     * =========================================================
     */

    private List<A4LayoutPlacement> placeSingleA5(
            A4LayoutItem item,
            int pageNumber
    ) {

        float contentWidth =
                layoutService.getContentWidth();

        float contentHeight =
                layoutService.getContentHeight();


        /*
         * Zuerst zwingend Querformat versuchen.
         */
        Candidate landscape =
                createLandscapeCandidate(
                        item,
                        pageNumber,
                        layoutService.getMarginLeft(),
                        layoutService.getMarginBottom(),
                        contentWidth,
                        contentHeight
                );


        if (landscape != null) {

            return List.of(
                    landscape.placement()
            );
        }


        /*
         * Nur wenn Querformat nicht möglich ist,
         * darf ein alleine stehendes A5 hochkant
         * verwendet werden.
         */
        Candidate portrait =
                createCandidateInArea(
                        item,
                        pageNumber,
                        layoutService.getMarginLeft(),
                        layoutService.getMarginBottom(),
                        contentWidth,
                        contentHeight,
                        false
                );


        if (portrait != null) {

            return List.of(
                    portrait.placement()
            );
        }


        return splitItem(
                item,
                pageNumber
        );
    }


    /*
     * =========================================================
     * 8 x A7
     * =========================================================
     */

    private List<A4LayoutPlacement> placeEightA7(
            A4LayoutItem first,
            A4LayoutItem second,
            A4LayoutItem third,
            A4LayoutItem fourth,
            A4LayoutItem fifth,
            A4LayoutItem sixth,
            A4LayoutItem seventh,
            A4LayoutItem eighth,
            int pageNumber
    ) {

        List<A4LayoutItem> a7Items =
                List.of(
                        first,
                        second,
                        third,
                        fourth,
                        fifth,
                        sixth,
                        seventh,
                        eighth
                );

        LayoutCandidate candidate =
                createEightA7Layout(
                        a7Items,
                        pageNumber
                );

        if (candidate == null) {
            return null;
        }

        return candidate.placements();
    }




    /*
     * =========================================================
     * A7-BLOCK
     * =========================================================
     */
    private List<A4LayoutPlacement> placeA7Block(
            List<A4LayoutItem> a7Items,
            int pageNumber
    ) {

        if (a7Items == null
                || a7Items.isEmpty()
                || a7Items.size() > 8) {
            return null;
        }

        float contentWidth =
                layoutService.getContentWidth();

        float contentHeight =
                layoutService.getContentHeight();

        float left =
                layoutService.getMarginLeft();

        float bottom =
                layoutService.getMarginBottom();

        int count = a7Items.size();

        /*
         * =========================================================
         * Bis 4 A7:
         * immer 2 x 2 Raster
         *
         * A7 ausdrücklich Landscape.
         * =========================================================
         */
        if (count <= 4) {

            float cellWidth =
                    (contentWidth - GAP) / 2f;

            float cellHeight =
                    (contentHeight - GAP) / 2f;

            List<A4LayoutPlacement> result =
                    new ArrayList<>();

            for (int i = 0; i < count; i++) {

                int row = i / 2;
                int column = i % 2;

                float x =
                        left
                                + column
                                * (cellWidth + GAP);

                float y =
                        bottom
                                + (1 - row)
                                * (cellHeight + GAP);

                Candidate candidate =
                        createLandscapeCandidate(
                                a7Items.get(i),
                                pageNumber,
                                x,
                                y,
                                cellWidth,
                                cellHeight
                        );

                if (candidate == null) {
                    return null;
                }

                result.add(
                        candidate.placement()
                );
            }

            return result;
        }


        /*
         * =========================================================
         * 5–8 A7:
         * 2 x 4 Raster
         * =========================================================
         */

        int columns = 2;

        int rows = (int) Math.ceil(
                count / 2.0
        );

        float cellWidth =
                (contentWidth - GAP) / 2f;

        float cellHeight =
                (contentHeight
                        - (rows - 1) * GAP)
                        / rows;

        List<A4LayoutPlacement> result =
                new ArrayList<>();

        for (int i = 0; i < count; i++) {

            int row = i / 2;
            int column = i % 2;

            float x =
                    left
                            + column
                            * (cellWidth + GAP);

            float y =
                    bottom
                            + (rows - 1 - row)
                            * (cellHeight + GAP);

            Candidate candidate =
                    createLandscapeCandidate(
                            a7Items.get(i),
                            pageNumber,
                            x,
                            y,
                            cellWidth,
                            cellHeight
                    );

            if (candidate == null) {
                return null;
            }

            result.add(
                    candidate.placement()
            );
        }

        return result;
    }


    /*
     * =========================================================
     * 4 x A7 + A5 QUER
     * =========================================================
     *
     * Die vier A7 liegen oben nebeneinander.
     * Das A5 liegt darunter und wird quer ausgerichtet.
     *
     * Die verfügbare Höhe wird zwischen beiden Bereichen
     * aufgeteilt. Dadurch werden die vier A7 gemeinsam
     * skaliert, ohne dass ein A7 gegenüber den anderen
     * eine andere Größe bekommt.
     */
    private List<A4LayoutPlacement> placeFourA7PlusA5(
            A4LayoutItem first,
            A4LayoutItem second,
            A4LayoutItem third,
            A4LayoutItem fourth,
            A4LayoutItem a5,
            int pageNumber
    ) {
        float contentWidth =
                layoutService.getContentWidth();

        float contentHeight =
                layoutService.getContentHeight();

        float left =
                layoutService.getMarginLeft();

        float bottom =
                layoutService.getMarginBottom();

        /*
         * A5 zunächst auf die zulässige Mindestgröße bringen.
         * Danach bekommt A7 den verbleibenden Bereich.
         */
        float a5Scale =
                calculateLandscapeScale(
                        a5,
                        contentWidth,
                        contentHeight
                );

        if (a5Scale < MIN_SCALE) {
            return null;
        }

        float a5Height =
                a5.width() * a5Scale;

        float a7AreaHeight =
                contentHeight
                        - GAP
                        - a5Height;

        if (a7AreaHeight <= 0) {
            return null;
        }

        float a7CellWidth =
                (contentWidth - 3f * GAP) / 4f;

        List<A4LayoutItem> a7Items =
                List.of(
                        first,
                        second,
                        third,
                        fourth
                );

        List<A4LayoutPlacement> result =
                new ArrayList<>();

        for (int i = 0; i < a7Items.size(); i++) {

            Candidate candidate =
                    createLandscapeCandidate(
                            a7Items.get(i),
                            pageNumber,
                            left + i
                                    * (a7CellWidth + GAP),
                            bottom
                                    + a5Height
                                    + GAP,
                            a7CellWidth,
                            a7AreaHeight
                    );

            if (candidate == null) {
                return null;
            }

            result.add(candidate.placement());
        }

        Candidate a5Candidate =
                createLandscapeCandidate(
                        a5,
                        pageNumber,
                        left,
                        bottom,
                        contentWidth,
                        a5Height
                );

        if (a5Candidate == null) {
            return null;
        }

        result.add(a5Candidate.placement());

        return result;
    }


    private float calculateLandscapeScale(
            A4LayoutItem item,
            float availableWidth,
            float availableHeight
    ) {
        float sourceWidth = item.height();
        float sourceHeight = item.width();

        float scaleX =
                availableWidth / sourceWidth;

        float scaleY =
                availableHeight / sourceHeight;

        return Math.min(
                Math.min(scaleX, scaleY),
                MAX_SCALE
        );
    }


    private List<A4LayoutPlacement> placeA5PlusTwoA6(
            A4LayoutItem first,
            A4LayoutItem second,
            A4LayoutItem third,
            int pageNumber
    ) {

        A4LayoutItem a5 =
                isA5(first)
                        ? first
                        : isA5(second)
                        ? second
                        : third;

        List<A4LayoutItem> a6Items =
                new ArrayList<>();

        if (isA6(first)) {
            a6Items.add(first);
        }

        if (isA6(second)) {
            a6Items.add(second);
        }

        if (isA6(third)) {
            a6Items.add(third);
        }

        if (a6Items.size() != 2) {
            return null;
        }

        float contentWidth =
                layoutService.getContentWidth();

        float contentHeight =
                layoutService.getContentHeight();

        float left =
                layoutService.getMarginLeft();

        float bottom =
                layoutService.getMarginBottom();


        /*
         * =========================================================
         * A5 oben
         * 2 x A6 unten
         * =========================================================
         *
         * A5 quer und die beiden A6 hoch haben dasselbe
         * Seitenverhältnis bezogen auf die gemeinsame Höhe.
         *
         * Deshalb wird die verfügbare Höhe gleichmäßig auf
         * A5 und A6-Bereich verteilt.
         */

        float areaHeight =
                (contentHeight - GAP) / 2f;


        float a6Width =
                (contentWidth - GAP) / 2f;


        /*
         * A5
         */
        Candidate a5Candidate =
                createLandscapeCandidate(
                        a5,
                        pageNumber,
                        left,
                        bottom + areaHeight + GAP,
                        contentWidth,
                        areaHeight
                );


        /*
         * A6 links
         */
        Candidate a6First =
                createPortraitCandidate(
                        a6Items.get(0),
                        pageNumber,
                        left,
                        bottom,
                        a6Width,
                        areaHeight
                );


        /*
         * A6 rechts
         */
        Candidate a6Second =
                createPortraitCandidate(
                        a6Items.get(1),
                        pageNumber,
                        left + a6Width + GAP,
                        bottom,
                        a6Width,
                        areaHeight
                );


        if (a5Candidate == null
                || a6First == null
                || a6Second == null) {

            return null;
        }


        /*
         * =========================================================
         * Reihenfolge der ursprünglichen Dokumente erhalten
         * =========================================================
         */

        List<A4LayoutPlacement> placements =
                new ArrayList<>(3);

        for (A4LayoutItem item :
                List.of(first, second, third)) {

            if (item == a5) {

                placements.add(
                        a5Candidate.placement()
                );

            } else if (item == a6Items.getFirst()) {

                placements.add(
                        a6First.placement()
                );

            } else {

                placements.add(
                        a6Second.placement()
                );
            }
        }

        return placements;
    }


    /*
     * =========================================================
     * A5 + A6
     * =========================================================
     */

    private List<A4LayoutPlacement> placeA5PlusA6(
            A4LayoutItem first,
            A4LayoutItem second,
            int pageNumber
    ) {

        A4LayoutItem a5 =
                isA5(first)
                        ? first
                        : second;

        A4LayoutItem a6 =
                isA6(first)
                        ? first
                        : second;

        float contentWidth =
                layoutService.getContentWidth();

        float contentHeight =
                layoutService.getContentHeight();

        float a5Height =
                (contentHeight - GAP) * 0.60f;

        float a6Height =
                contentHeight
                        - GAP
                        - a5Height;

        float left =
                layoutService.getMarginLeft();

        float bottom =
                layoutService.getMarginBottom();


        Candidate a5Candidate =
                createLandscapeCandidate(
                        a5,
                        pageNumber,
                        left,
                        bottom
                                + a6Height
                                + GAP,
                        contentWidth,
                        a5Height
                );


        Candidate a6Candidate =
                createLandscapeCandidate(
                        a6,
                        pageNumber,
                        left,
                        bottom,
                        contentWidth,
                        a6Height
                );


        if (a5Candidate == null
                || a6Candidate == null) {

            return null;
        }


        if (first == a5) {

            return List.of(
                    a5Candidate.placement(),
                    a6Candidate.placement()
            );

        }

        return List.of(
                a6Candidate.placement(),
                a5Candidate.placement()
        );
    }


    private List<A4LayoutPlacement> placeTwoA6PlusFourA7(
            A4LayoutItem first,
            A4LayoutItem second,
            A4LayoutItem third,
            A4LayoutItem fourth,
            A4LayoutItem fifth,
            A4LayoutItem sixth,
            int pageNumber
    ) {

        float contentWidth =
                layoutService.getContentWidth();

        float contentHeight =
                layoutService.getContentHeight();

        float left =
                layoutService.getMarginLeft();

        float bottom =
                layoutService.getMarginBottom();

        float cellWidth =
                (contentWidth - GAP) / 2f;


        /*
         * =========================================================
         * Variante 1
         * =========================================================
         *
         * A6 oben
         * 2 x 2 A7 darunter
         */
        float a6AreaHeight =
                (contentHeight - GAP) * 0.50f;

        float a7AreaHeight =
                contentHeight
                        - GAP
                        - a6AreaHeight;

        float a7CellHeight =
                (a7AreaHeight - GAP) / 2f;


        List<LayoutSlot> topA6BottomA7Slots =
                List.of(

                        /*
                         * A6 links oben
                         */
                        new LayoutSlot(
                                left,
                                bottom
                                        + a7AreaHeight
                                        + GAP,
                                cellWidth,
                                a6AreaHeight,
                                LayoutOrientation.PORTRAIT
                        ),

                        /*
                         * A6 rechts oben
                         */
                        new LayoutSlot(
                                left
                                        + cellWidth
                                        + GAP,
                                bottom
                                        + a7AreaHeight
                                        + GAP,
                                cellWidth,
                                a6AreaHeight,
                                LayoutOrientation.PORTRAIT
                        ),

                        /*
                         * A7 links oben
                         */
                        new LayoutSlot(
                                left,
                                bottom
                                        + a7CellHeight
                                        + GAP,
                                cellWidth,
                                a7CellHeight,
                                LayoutOrientation.LANDSCAPE
                        ),

                        /*
                         * A7 rechts oben
                         */
                        new LayoutSlot(
                                left
                                        + cellWidth
                                        + GAP,
                                bottom
                                        + a7CellHeight
                                        + GAP,
                                cellWidth,
                                a7CellHeight,
                                LayoutOrientation.LANDSCAPE
                        ),

                        /*
                         * A7 links unten
                         */
                        new LayoutSlot(
                                left,
                                bottom,
                                cellWidth,
                                a7CellHeight,
                                LayoutOrientation.LANDSCAPE
                        ),

                        /*
                         * A7 rechts unten
                         */
                        new LayoutSlot(
                                left
                                        + cellWidth
                                        + GAP,
                                bottom,
                                cellWidth,
                                a7CellHeight,
                                LayoutOrientation.LANDSCAPE
                        )
                );


        LayoutCandidate variantOne =
                createLayoutCandidate(
                        List.of(
                                first,
                                second,
                                third,
                                fourth,
                                fifth,
                                sixth
                        ),
                        topA6BottomA7Slots,
                        pageNumber
                );


        /*
         * =========================================================
         * Variante 2
         * =========================================================
         *
         * 2 x 2 A7 oben
         * A6 unten
         */
        float a7AreaHeightTop =
                (contentHeight - GAP) * 0.50f;

        float a6AreaHeightBottom =
                contentHeight
                        - GAP
                        - a7AreaHeightTop;

        float a7CellHeightTop =
                (a7AreaHeightTop - GAP) / 2f;


        List<LayoutSlot> topA7BottomA6Slots =
                List.of(

                        /*
                         * A6 links unten
                         */
                        new LayoutSlot(
                                left,
                                bottom,
                                cellWidth,
                                a6AreaHeightBottom,
                                LayoutOrientation.PORTRAIT
                        ),

                        /*
                         * A6 rechts unten
                         */
                        new LayoutSlot(
                                left
                                        + cellWidth
                                        + GAP,
                                bottom,
                                cellWidth,
                                a6AreaHeightBottom,
                                LayoutOrientation.PORTRAIT
                        ),

                        /*
                         * A7 links oben
                         */
                        new LayoutSlot(
                                left,
                                bottom
                                        + a6AreaHeightBottom
                                        + GAP
                                        + a7CellHeightTop,
                                cellWidth,
                                a7CellHeightTop,
                                LayoutOrientation.LANDSCAPE
                        ),

                        /*
                         * A7 rechts oben
                         */
                        new LayoutSlot(
                                left
                                        + cellWidth
                                        + GAP,
                                bottom
                                        + a6AreaHeightBottom
                                        + GAP
                                        + a7CellHeightTop,
                                cellWidth,
                                a7CellHeightTop,
                                LayoutOrientation.LANDSCAPE
                        ),

                        /*
                         * A7 links Mitte
                         */
                        new LayoutSlot(
                                left,
                                bottom
                                        + a6AreaHeightBottom
                                        + GAP,
                                cellWidth,
                                a7CellHeightTop,
                                LayoutOrientation.LANDSCAPE
                        ),

                        /*
                         * A7 rechts Mitte
                         */
                        new LayoutSlot(
                                left
                                        + cellWidth
                                        + GAP,
                                bottom
                                        + a6AreaHeightBottom
                                        + GAP,
                                cellWidth,
                                a7CellHeightTop,
                                LayoutOrientation.LANDSCAPE
                        )
                );


        LayoutCandidate variantTwo =
                createLayoutCandidate(
                        List.of(
                                first,
                                second,
                                third,
                                fourth,
                                fifth,
                                sixth
                        ),
                        topA7BottomA6Slots,
                        pageNumber
                );


        /*
         * Die Variante mit der besseren Mindestskalierung
         * gewinnt.
         */
        LayoutCandidate selected =
                selectBestLayout(
                        variantOne,
                        variantTwo
                );


        if (selected == null) {
            return null;
        }

        return selected.placements();
    }

    /*
     * =========================================================
     * 4 x A6
     * =========================================================
     */

    private List<A4LayoutPlacement> placeFourA6(
            A4LayoutItem first,
            A4LayoutItem second,
            A4LayoutItem third,
            A4LayoutItem fourth,
            int pageNumber
    ) {

        float contentWidth =
                layoutService.getContentWidth();

        float contentHeight =
                layoutService.getContentHeight();

        float left =
                layoutService.getMarginLeft();

        float bottom =
                layoutService.getMarginBottom();


        /*
         * =========================================================
         * 2 x 2 Raster
         * =========================================================
         */

        float cellWidth =
                (contentWidth - GAP) / 2f;

        float cellHeight =
                (contentHeight - GAP) / 2f;


        /*
         * Obere Reihe
         */
        Candidate firstCandidate =
                createPortraitCandidate(
                        first,
                        pageNumber,
                        left,
                        bottom + cellHeight + GAP,
                        cellWidth,
                        cellHeight
                );

        Candidate secondCandidate =
                createPortraitCandidate(
                        second,
                        pageNumber,
                        left + cellWidth + GAP,
                        bottom + cellHeight + GAP,
                        cellWidth,
                        cellHeight
                );


        /*
         * Untere Reihe
         */
        Candidate thirdCandidate =
                createPortraitCandidate(
                        third,
                        pageNumber,
                        left,
                        bottom,
                        cellWidth,
                        cellHeight
                );

        Candidate fourthCandidate =
                createPortraitCandidate(
                        fourth,
                        pageNumber,
                        left + cellWidth + GAP,
                        bottom,
                        cellWidth,
                        cellHeight
                );


        /*
         * Wenn auch nur ein A6 unter die Mindestskalierung
         * fällt, ist dieses Layout nicht zulässig.
         */
        if (firstCandidate == null
                || secondCandidate == null
                || thirdCandidate == null
                || fourthCandidate == null) {

            return null;
        }


        /*
         * Die ursprüngliche Reihenfolge bleibt erhalten.
         */
        return List.of(
                firstCandidate.placement(),
                secondCandidate.placement(),
                thirdCandidate.placement(),
                fourthCandidate.placement()
        );
    }


    /*
     * =========================================================
     * 2 x A6
     * =========================================================
     */

    private List<A4LayoutPlacement> placeTwoA6(
            A4LayoutItem first,
            A4LayoutItem second,
            int pageNumber
    ) {

        float contentWidth =
                layoutService.getContentWidth();

        float contentHeight =
                layoutService.getContentHeight();

        float cellWidth =
                (contentWidth - GAP) / 2f;

        float left =
                layoutService.getMarginLeft();

        float bottom =
                layoutService.getMarginBottom();


        Candidate firstCandidate =
                createPortraitCandidate(
                        first,
                        pageNumber,
                        left,
                        bottom,
                        cellWidth,
                        contentHeight
                );


        Candidate secondCandidate =
                createPortraitCandidate(
                        second,
                        pageNumber,
                        left
                                + cellWidth
                                + GAP,
                        bottom,
                        cellWidth,
                        contentHeight
                );


        if (firstCandidate == null
                || secondCandidate == null) {

            return null;
        }


        return List.of(
                firstCandidate.placement(),
                secondCandidate.placement()
        );
    }


    /*
     * =========================================================
     * QUERFORMAT-KANDIDAT
     * =========================================================
     */

    private Candidate createLandscapeCandidate(
            A4LayoutItem item,
            int pageNumber,
            float x,
            float y,
            float availableWidth,
            float availableHeight
    ) {

        Candidate normal =
                createCandidateInArea(
                        item,
                        pageNumber,
                        x,
                        y,
                        availableWidth,
                        availableHeight,
                        false
                );

        Candidate rotated =
                createCandidateInArea(
                        item,
                        pageNumber,
                        x,
                        y,
                        availableWidth,
                        availableHeight,
                        true
                );

        Candidate best = null;

        if (normal != null
                && normal.placement().width()
                >= normal.placement().height()) {

            best = normal;
        }

        if (rotated != null
                && rotated.placement().width()
                >= rotated.placement().height()) {

            if (best == null
                    || rotated.scale() > best.scale()) {

                best = rotated;
            }
        }

        return best;
    }

    /*
     * =========================================================
     * HOCHFORMAT-KANDIDAT
     * =========================================================
     *
     * Das Dokument wird so platziert, dass es auf der
     * A4-Seite hochkant erscheint.
     *
     * Ist das Quelldokument selbst quer aufgenommen,
     * wird es technisch um 90° gedreht.
     */
    private Candidate createPortraitCandidate(
            A4LayoutItem item,
            int pageNumber,
            float x,
            float y,
            float availableWidth,
            float availableHeight
    ) {

        Candidate normal =
                createCandidateInArea(
                        item,
                        pageNumber,
                        x,
                        y,
                        availableWidth,
                        availableHeight,
                        false
                );

        Candidate rotated =
                createCandidateInArea(
                        item,
                        pageNumber,
                        x,
                        y,
                        availableWidth,
                        availableHeight,
                        true
                );

        Candidate best = null;

        if (normal != null
                && normal.placement().height()
                >= normal.placement().width()) {

            best = normal;
        }

        if (rotated != null
                && rotated.placement().height()
                >= rotated.placement().width()) {

            if (best == null
                    || rotated.scale() > best.scale()) {

                best = rotated;
            }
        }

        return best;
    }
    /*
     * =========================================================
     * NORMALES PAAR
     * =========================================================
     */

    private List<A4LayoutPlacement> placePair(
            A4LayoutItem first,
            A4LayoutItem second,
            int pageNumber
    ) {

        float contentWidth =
                layoutService.getContentWidth();

        float contentHeight =
                layoutService.getContentHeight();


        /*
         * Zwei Dokumente untereinander.
         */
        float cellHeight =
                (contentHeight - GAP) / 2f;


        CandidatePair vertical =
                createPairCandidate(
                        first,
                        second,
                        pageNumber,

                        layoutService.getMarginLeft(),
                        layoutService.getMarginBottom()
                                + cellHeight
                                + GAP,
                        contentWidth,
                        cellHeight,

                        layoutService.getMarginLeft(),
                        layoutService.getMarginBottom(),
                        contentWidth,
                        cellHeight
                );


        /*
         * Zwei Dokumente nebeneinander.
         */
        float cellWidth =
                (contentWidth - GAP) / 2f;


        CandidatePair horizontal =
                createPairCandidate(
                        first,
                        second,
                        pageNumber,

                        layoutService.getMarginLeft(),
                        layoutService.getMarginBottom(),
                        cellWidth,
                        contentHeight,

                        layoutService.getMarginLeft()
                                + cellWidth
                                + GAP,
                        layoutService.getMarginBottom(),
                        cellWidth,
                        contentHeight
                );


        CandidatePair selected =
                selectBestPair(
                        vertical,
                        horizontal
                );


        if (selected == null) {
            return null;
        }


        return selected.placements();
    }


    private CandidatePair createPairCandidate(
            A4LayoutItem first,
            A4LayoutItem second,
            int pageNumber,

            float firstX,
            float firstY,
            float firstWidth,
            float firstHeight,

            float secondX,
            float secondY,
            float secondWidth,
            float secondHeight
    ) {

        Candidate firstNormal =
                createCandidateInArea(
                        first,
                        pageNumber,
                        firstX,
                        firstY,
                        firstWidth,
                        firstHeight,
                        false
                );


        Candidate firstRotated =
                createCandidateInArea(
                        first,
                        pageNumber,
                        firstX,
                        firstY,
                        firstWidth,
                        firstHeight,
                        true
                );


        Candidate firstCandidate =
                selectBestCandidate(
                        firstNormal,
                        firstRotated
                );


        Candidate secondNormal =
                createCandidateInArea(
                        second,
                        pageNumber,
                        secondX,
                        secondY,
                        secondWidth,
                        secondHeight,
                        false
                );


        Candidate secondRotated =
                createCandidateInArea(
                        second,
                        pageNumber,
                        secondX,
                        secondY,
                        secondWidth,
                        secondHeight,
                        true
                );


        Candidate secondCandidate =
                selectBestCandidate(
                        secondNormal,
                        secondRotated
                );


        if (firstCandidate == null
                || secondCandidate == null) {

            return null;
        }


        return new CandidatePair(
                List.of(
                        firstCandidate.placement(),
                        secondCandidate.placement()
                ),
                Math.min(
                        firstCandidate.scale(),
                        secondCandidate.scale()
                )
        );
    }

    private LayoutCandidate createLayoutCandidate(
            List<A4LayoutItem> items,
            List<LayoutSlot> slots,
            int pageNumber
    ) {

        if (items == null
                || slots == null
                || items.size() != slots.size()) {

            return null;
        }

        List<A4LayoutPlacement> placements =
                new ArrayList<>();

        float minimumScale =
                MAX_SCALE;

        for (int i = 0; i < items.size(); i++) {

            A4LayoutItem item =
                    items.get(i);

            LayoutSlot slot =
                    slots.get(i);

            Candidate candidate;

            if (slot.orientation()
                    == LayoutOrientation.LANDSCAPE) {

                candidate =
                        createLandscapeCandidate(
                                item,
                                pageNumber,
                                slot.x(),
                                slot.y(),
                                slot.width(),
                                slot.height()
                        );

            } else {

                candidate =
                        createPortraitCandidate(
                                item,
                                pageNumber,
                                slot.x(),
                                slot.y(),
                                slot.width(),
                                slot.height()
                        );
            }

            if (candidate == null) {
                return null;
            }

            placements.add(
                    candidate.placement()
            );

            minimumScale =
                    Math.min(
                            minimumScale,
                            candidate.scale()
                    );
        }

        return new LayoutCandidate(
                placements,
                minimumScale
        );
    }

    private LayoutCandidate selectBestLayout(
            LayoutCandidate current,
            LayoutCandidate candidate
    ) {

        if (candidate == null) {
            return current;
        }

        if (current == null) {
            return candidate;
        }

        return candidate.minimumScale()
                > current.minimumScale()
                ? candidate
                : current;
    }


    private LayoutCandidate createEightA7Layout(
            List<A4LayoutItem> items,
            int pageNumber
    ) {

        if (items.size() != 8) {
            return null;
        }

        float contentWidth =
                layoutService.getContentWidth();

        float contentHeight =
                layoutService.getContentHeight();

        float left =
                layoutService.getMarginLeft();

        float bottom =
                layoutService.getMarginBottom();

        /*
         * =========================================================
         * 2 Spalten × 4 Reihen
         * =========================================================
         *
         * A7 wird quer ausgegeben.
         */
        float cellWidth =
                (contentWidth - GAP) / 2f;

        float cellHeight =
                (contentHeight - 3f * GAP) / 4f;

        List<LayoutSlot> slots =
                new ArrayList<>(8);

        for (int row = 0; row < 4; row++) {

            for (int column = 0; column < 2; column++) {

                float x =
                        left
                                + column
                                * (cellWidth + GAP);

                float y =
                        bottom
                                + (3 - row)
                                * (cellHeight + GAP);

                slots.add(
                        new LayoutSlot(
                                x,
                                y,
                                cellWidth,
                                cellHeight,
                                LayoutOrientation.LANDSCAPE
                        )
                );
            }
        }

        return createLayoutCandidate(
                items,
                slots,
                pageNumber
        );
    }
    /*
     * =========================================================
     * CANDIDATE
     * =========================================================
     */

    private Candidate createCandidate(
            A4LayoutItem item,
            int pageNumber,
            float availableWidth,
            float availableHeight,
            boolean rotated
    ) {

        return createCandidateInArea(
                item,
                pageNumber,
                layoutService.getMarginLeft(),
                layoutService.getMarginBottom(),
                availableWidth,
                availableHeight,
                rotated
        );
    }


    private Candidate createCandidateInArea(
            A4LayoutItem item,
            int pageNumber,
            float x,
            float y,
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


        if (scale < MIN_SCALE) {
            return null;
        }

        float width =
                sourceWidth * scale;

        float height =
                sourceHeight * scale;


        float outputX =
                x
                        + (availableWidth - width) / 2f;

        float outputY =
                y
                        + (availableHeight - height) / 2f;


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
     * =========================================================
     * AUSWAHL
     * =========================================================
     */

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

        return rotated.scale() > normal.scale()
                ? rotated
                : normal;
    }


    private CandidatePair selectBestPair(
            CandidatePair first,
            CandidatePair second
    ) {

        if (first == null) {
            return second;
        }

        if (second == null) {
            return first;
        }

        return second.minimumScale()
                > first.minimumScale()
                ? second
                : first;
    }



    /*
     * =========================================================
     * SPLIT
     * =========================================================
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
     * =========================================================
     * HILFSMETHODEN
     * =========================================================
     */

    private int nextPageNumber(
            List<A4LayoutPlacement> placements,
            int currentPage
    ) {

        return placements.stream()
                .mapToInt(
                        A4LayoutPlacement::pageNumber
                )
                .max()
                .orElse(currentPage)
                + 1;
    }


    /*
     * =========================================================
     * INTERN
     * =========================================================
     */

    private record Candidate(
            A4LayoutPlacement placement,
            float scale
    ) {
    }


    private record CandidatePair(
            List<A4LayoutPlacement> placements,
            float minimumScale
    ) {
    }

    private List<A4LayoutPlacement> placeA6PlusTwoA7(
            A4LayoutItem first,
            A4LayoutItem second,
            A4LayoutItem third,
            int pageNumber
    ) {

        A4LayoutItem a6 =
                isA6(first)
                        ? first
                        : isA6(second)
                        ? second
                        : third;

        List<A4LayoutItem> a7Items =
                new ArrayList<>();

        if (isA7(first)) {
            a7Items.add(first);
        }

        if (isA7(second)) {
            a7Items.add(second);
        }

        if (isA7(third)) {
            a7Items.add(third);
        }

        if (a7Items.size() != 2) {
            return null;
        }

        float contentWidth =
                layoutService.getContentWidth();

        float contentHeight =
                layoutService.getContentHeight();

        float left =
                layoutService.getMarginLeft();

        float bottom =
                layoutService.getMarginBottom();


        /*
         * =========================================================
         * A6 oben
         * 2 x A7 unten
         * =========================================================
         */

        float areaHeight =
                (contentHeight - GAP) / 2f;

        float a7CellWidth =
                (contentWidth - GAP) / 2f;


        /*
         * A6 oben links/zentriert.
         *
         * Da A6 hochkant ist, bekommt es die gesamte obere
         * Hälfte als verfügbare Fläche.
         */
        Candidate a6Candidate =
                createPortraitCandidate(
                        a6,
                        pageNumber,
                        left,
                        bottom
                                + areaHeight
                                + GAP,
                        contentWidth,
                        areaHeight
                );


        /*
         * A7 links unten
         */
        Candidate a7First =
                createLandscapeCandidate(
                        a7Items.get(0),
                        pageNumber,
                        left,
                        bottom,
                        a7CellWidth,
                        areaHeight
                );


        /*
         * A7 rechts unten
         */
        Candidate a7Second =
                createLandscapeCandidate(
                        a7Items.get(1),
                        pageNumber,
                        left
                                + a7CellWidth
                                + GAP,
                        bottom,
                        a7CellWidth,
                        areaHeight
                );


        if (a6Candidate == null
                || a7First == null
                || a7Second == null) {

            return null;
        }


        /*
         * Ursprüngliche Reihenfolge erhalten.
         */
        List<A4LayoutPlacement> placements =
                new ArrayList<>(3);

        for (A4LayoutItem item :
                List.of(first, second, third)) {

            if (item == a6) {

                placements.add(
                        a6Candidate.placement()
                );

            } else if (item == a7Items.getFirst()) {

                placements.add(
                        a7First.placement()
                );

            } else {

                placements.add(
                        a7Second.placement()
                );
            }
        }

        return placements;
    }

    private PagePlan createTwoA6PlusFourA7Plan(
            List<A4LayoutItem> items,
            int pageNumber
    ) {

        List<A4LayoutItem> a6Items =
                items.stream()
                        .filter(this::isA6)
                        .toList();

        List<A4LayoutItem> a7Items =
                items.stream()
                        .filter(this::isA7)
                        .toList();

        if (a6Items.size() != 2
                || a7Items.size() != 4) {

            return null;
        }

        List<A4LayoutPlacement> placements =
                placeTwoA6PlusFourA7(
                        a6Items.get(0),
                        a6Items.get(1),
                        a7Items.get(0),
                        a7Items.get(1),
                        a7Items.get(2),
                        a7Items.get(3),
                        pageNumber
                );

        if (placements == null) {
            return null;
        }

        /*
         * Die Platzierungsmethode erzeugt die Placements
         * in Layout-Reihenfolge.
         *
         * Für die Engine stellen wir anschließend die
         * ursprüngliche Dokumentreihenfolge wieder her.
         */
        placements =
                restoreOriginalOrder(
                        items,
                        placements
                );

        return createPagePlan(
                items,
                placements
        );
    }

    private PagePlan createFourA7PlusA5Plan(
            List<A4LayoutItem> items,
            int pageNumber
    ) {

        A4LayoutItem a5 =
                items.stream()
                        .filter(this::isA5)
                        .findFirst()
                        .orElse(null);

        List<A4LayoutItem> a7Items =
                items.stream()
                        .filter(this::isA7)
                        .toList();

        if (a5 == null
                || a7Items.size() != 4) {

            return null;
        }

        List<A4LayoutPlacement> placements =
                placeFourA7PlusA5(
                        a7Items.get(0),
                        a7Items.get(1),
                        a7Items.get(2),
                        a7Items.get(3),
                        a5,
                        pageNumber
                );

        if (placements == null) {
            return null;
        }

        /*
         * placeFourA7PlusA5 erzeugt die Placements zunächst
         * als 4 × A7 und danach A5.
         *
         * Für die Engine muss aber die ursprüngliche
         * Dokumentreihenfolge erhalten bleiben.
         */
        placements =
                restoreOriginalOrder(
                        items,
                        placements
                );

        return createPagePlan(
                items,
                placements
        );
    }

    private List<A4LayoutPlacement> restoreOriginalOrder(
            List<A4LayoutItem> originalItems,
            List<A4LayoutPlacement> placements
    ) {

        return originalItems.stream()
                .map(
                        item -> placements.stream()
                                .filter(
                                        placement ->
                                                placement.itemId()
                                                        .equals(
                                                                item.id()
                                                        )
                                )
                                .findFirst()
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        "Kein Placement für Item "
                                                                + item.id()
                                                                + " gefunden."
                                                )
                                )
                )
                .toList();
    }

    private float getPlacementScale(
            A4LayoutItem item,
            A4LayoutPlacement placement
    ) {

        float sourceWidth =
                placement.rotation() == 90
                        ? item.height()
                        : item.width();

        return placement.width()
                / sourceWidth;
    }
}