package com.kcserver.service.pdf;

import com.kcserver.enumtype.PdfDocumentDensity;
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

    private float getMinimumScale(
            A4LayoutItem item
    ) {
        return MIN_SCALE;
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
             * =================================================
             * A4
             * =================================================
             *
             * A4 immer alleine.
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
             * =================================================
             * A5 + 2 x A6
             * =================================================
             *
             * Die ersten drei noch nicht verarbeiteten
             * Dokumente werden betrachtet.
             *
             * Reihenfolge kann z. B. sein:
             *
             * A5 A6 A6
             * A6 A5 A6
             * A6 A6 A5
             */
            if (index + 2 < items.size()) {

                A4LayoutItem first =
                        items.get(index);

                A4LayoutItem second =
                        items.get(index + 1);

                A4LayoutItem third =
                        items.get(index + 2);

                if (isA5A6A6(
                        first,
                        second,
                        third
                )) {

                    List<A4LayoutPlacement> combination =
                            placeA5PlusTwoA6(
                                    first,
                                    second,
                                    third,
                                    pageNumber
                            );

                    if (combination != null) {

                        result.addAll(combination);

                        pageNumber++;

                        index += 3;

                        continue;
                    }
                }
            }


            /*
             * =================================================
             * 4 x A6
             * =================================================
             */
            if (index + 3 < items.size()) {

                A4LayoutItem first =
                        items.get(index);

                A4LayoutItem second =
                        items.get(index + 1);

                A4LayoutItem third =
                        items.get(index + 2);

                A4LayoutItem fourth =
                        items.get(index + 3);

                if (isA6(first)
                        && isA6(second)
                        && isA6(third)
                        && isA6(fourth)) {

                    List<A4LayoutPlacement> fourA6 =
                            placeFourA6(
                                    first,
                                    second,
                                    third,
                                    fourth,
                                    pageNumber
                            );

                    if (fourA6 != null) {

                        result.addAll(fourA6);

                        pageNumber++;

                        index += 4;

                        continue;
                    }
                }
            }


            /*
             * =================================================
             * A5 + A6
             * =================================================
             *
             * Wenn keine A5 + 2 x A6 Kombination möglich ist,
             * versuchen wir A5 + A6.
             *
             * Die Reihenfolge der beiden Dokumente spielt
             * dabei keine Rolle.
             */
            if (index + 1 < items.size()) {

                A4LayoutItem first =
                        items.get(index);

                A4LayoutItem second =
                        items.get(index + 1);

                if (isA5A6(
                        first,
                        second
                )) {

                    List<A4LayoutPlacement> combination =
                            placeA5PlusA6(
                                    first,
                                    second,
                                    pageNumber
                            );

                    if (combination != null) {

                        result.addAll(combination);

                        pageNumber++;

                        index += 2;

                        continue;
                    }
                }
            }


            /*
             * =================================================
             * 2 x A6
             * =================================================
             */
            if (index + 1 < items.size()) {

                A4LayoutItem first =
                        items.get(index);

                A4LayoutItem second =
                        items.get(index + 1);

                if (isA6(first)
                        && isA6(second)) {

                    List<A4LayoutPlacement> pair =
                            placeTwoA6(
                                    first,
                                    second,
                                    pageNumber
                            );

                    if (pair != null) {

                        result.addAll(pair);

                        pageNumber++;

                        index += 2;

                        continue;
                    }
                }
            }


            /*
             * =================================================
             * A5 alleine
             * =================================================
             *
             * A5 wird quer bevorzugt.
             *
             * Hochformat ist nur hier erlaubt.
             */
            if (isA5(current)) {

                result.addAll(
                        placeSingleA5(
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
             * =================================================
             * Sonstige Dokumente
             * =================================================
             *
             * Für A7 / unbekannte Formate bleibt die
             * bisherige Paarlogik erhalten.
             */
            if (index + 1 < items.size()) {

                A4LayoutItem next =
                        items.get(index + 1);

                if (!isA4(next)
                        && next.density()
                        != PdfDocumentDensity.HIGH
                        && current.density()
                        != PdfDocumentDensity.HIGH) {

                    List<A4LayoutPlacement> pair =
                            placePair(
                                    current,
                                    next,
                                    pageNumber
                            );

                    if (pair != null) {

                        result.addAll(pair);

                        pageNumber++;

                        index += 2;

                        continue;
                    }
                }
            }


            /*
             * =================================================
             * Einzelnes Dokument
             * =================================================
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


    private boolean isA5A6(
            A4LayoutItem first,
            A4LayoutItem second
    ) {

        return (isA5(first) && isA6(second))
                || (isA6(first) && isA5(second));
    }


    private boolean isA5A6A6(
            A4LayoutItem first,
            A4LayoutItem second,
            A4LayoutItem third
    ) {

        int a5Count = 0;
        int a6Count = 0;

        if (isA5(first)) {
            a5Count++;
        }

        if (isA5(second)) {
            a5Count++;
        }

        if (isA5(third)) {
            a5Count++;
        }

        if (isA6(first)) {
            a6Count++;
        }

        if (isA6(second)) {
            a6Count++;
        }

        if (isA6(third)) {
            a6Count++;
        }

        return a5Count == 1
                && a6Count == 2;
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
     * A5 + 2 x A6
     * =========================================================
     */

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

        /*
         * Oberer Bereich für A5.
         */
        float a5Height =
                (contentHeight - GAP) * 0.55f;

        /*
         * Unterer Bereich für A6.
         */
        float a6AreaHeight =
                contentHeight
                        - GAP
                        - a5Height;

        float a6Width =
                (contentWidth - GAP) / 2f;

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
                                + a6AreaHeight
                                + GAP,
                        contentWidth,
                        a5Height
                );


        Candidate a6First =
                createPortraitCandidate(
                        a6Items.get(0),
                        pageNumber,
                        left,
                        bottom,
                        a6Width,
                        a6AreaHeight
                );

        Candidate a6Second =
                createPortraitCandidate(
                        a6Items.get(1),
                        pageNumber,
                        left
                                + a6Width
                                + GAP,
                        bottom,
                        a6Width,
                        a6AreaHeight
                );

        if (a5Candidate == null
                || a6First == null
                || a6Second == null) {

            return null;
        }


        /*
         * Die Reihenfolge der Dokumente bleibt
         * in der Placement-Liste erhalten.
         */
        List<A4LayoutPlacement> placements =
                new ArrayList<>();

        for (A4LayoutItem item :
                List.of(first, second, third)) {

            if (item == a5) {

                placements.add(
                        a5Candidate.placement()
                );

            } else if (item == a6Items.get(0)) {

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

        float cellWidth =
                (contentWidth - GAP) / 2f;

        float cellHeight =
                (contentHeight - GAP) / 2f;

        float left =
                layoutService.getMarginLeft();

        float bottom =
                layoutService.getMarginBottom();


        Candidate a =
                createPortraitCandidate(
                        first,
                        pageNumber,
                        left,
                        bottom
                                + cellHeight
                                + GAP,
                        cellWidth,
                        cellHeight
                );

        Candidate b =
                createPortraitCandidate(
                        second,
                        pageNumber,
                        left
                                + cellWidth
                                + GAP,
                        bottom
                                + cellHeight
                                + GAP,
                        cellWidth,
                        cellHeight
                );

        Candidate c =
                createPortraitCandidate(
                        third,
                        pageNumber,
                        left,
                        bottom,
                        cellWidth,
                        cellHeight
                );

        Candidate d =
                createPortraitCandidate(
                        fourth,
                        pageNumber,
                        left
                                + cellWidth
                                + GAP,
                        bottom,
                        cellWidth,
                        cellHeight
                );


        if (a == null
                || b == null
                || c == null
                || d == null) {

            return null;
        }


        return List.of(
                a.placement(),
                b.placement(),
                c.placement(),
                d.placement()
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


        if (scale < getMinimumScale(item)) {
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
}