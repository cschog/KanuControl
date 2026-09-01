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
             * 8 x A7
             * =================================================
             *
             * A7-Dokumente werden auf einer A4-Seite im
             * 2 x 4 Raster platziert. Die A7-Dokumente werden
             * dabei technisch um 90° gedreht, sodass sie als
             * Querformat in das Raster passen.
             *
             * Die Prüfung erfolgt vor A5/A6-Kombinationen, damit
             * eine Folge von acht A7 nicht durch die allgemeine
             * Paarlogik auf mehrere Seiten verteilt wird.
             */
            if (index + 7 < items.size()) {

                A4LayoutItem first = items.get(index);
                A4LayoutItem second = items.get(index + 1);
                A4LayoutItem third = items.get(index + 2);
                A4LayoutItem fourth = items.get(index + 3);
                A4LayoutItem fifth = items.get(index + 4);
                A4LayoutItem sixth = items.get(index + 5);
                A4LayoutItem seventh = items.get(index + 6);
                A4LayoutItem eighth = items.get(index + 7);

                if (isA7(first)
                        && isA7(second)
                        && isA7(third)
                        && isA7(fourth)
                        && isA7(fifth)
                        && isA7(sixth)
                        && isA7(seventh)
                        && isA7(eighth)) {

                    List<A4LayoutPlacement> eightA7 =
                            placeEightA7(
                                    first,
                                    second,
                                    third,
                                    fourth,
                                    fifth,
                                    sixth,
                                    seventh,
                                    eighth,
                                    pageNumber
                            );

                    if (eightA7 != null) {

                        result.addAll(eightA7);

                        pageNumber++;
                        index += 8;

                        continue;
                    }
                }
            }

            /*
             * =================================================
             * A5 + 2 x A7
             * =================================================
             *
             * Die Kombination wird unabhängig von der
             * ursprünglichen Reihenfolge erkannt:
             *
             * A5 A7 A7
             * A7 A5 A7
             * A7 A7 A5
             *
             * Alle drei Dokumente werden auf einer A4-Seite
             * platziert.
             */
            if (index + 2 < items.size()) {

                A4LayoutItem first =
                        items.get(index);

                A4LayoutItem second =
                        items.get(index + 1);

                A4LayoutItem third =
                        items.get(index + 2);

                if (isA5A7A7(
                        first,
                        second,
                        third
                )) {

                    List<A4LayoutPlacement> combination =
                            placeA5PlusTwoA7(
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
             * 1 x A6 + 2 x A7
             * =================================================
             *
             * Reihenfolge egal:
             *
             * A6 A7 A7
             * A7 A6 A7
             * A7 A7 A6
             */
            if (index + 2 < items.size()) {

                A4LayoutItem first =
                        items.get(index);

                A4LayoutItem second =
                        items.get(index + 1);

                A4LayoutItem third =
                        items.get(index + 2);

                if (isA6A7A7(
                        first,
                        second,
                        third
                )) {

                    List<A4LayoutPlacement> combination =
                            placeA6PlusTwoA7(
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
             * A7-BLOCK
             * =================================================
             *
             * Zusammenhängende A7 werden gemeinsam gepackt.
             * Maximal 8 A7 werden auf einer Seite angeordnet.
             *
             * Bei 4 A7 wird zuerst geprüft, ob unmittelbar
             * danach ein A5 oder 2 x A6 folgen. Diese Kombination
             * wird gemeinsam auf einer Seite geplant.
             */
            if (isA7(current)) {

                int a7Count = 0;

                while (index + a7Count < items.size()
                        && a7Count < 8
                        && isA7(items.get(index + a7Count))) {
                    a7Count++;
                }

                /*
                 * 4 x A7 + A5
                 */
                if (a7Count == 4
                        && index + 4 < items.size()
                        && isA5(items.get(index + 4))) {

                    List<A4LayoutPlacement> combination =
                            placeFourA7PlusA5(
                                    items.get(index),
                                    items.get(index + 1),
                                    items.get(index + 2),
                                    items.get(index + 3),
                                    items.get(index + 4),
                                    pageNumber
                            );

                    if (combination != null) {
                        result.addAll(combination);
                        pageNumber++;
                        index += 5;
                        continue;
                    }
                }

                /*
                 * 4 x A7 + 2 x A6
                 */
                if (a7Count == 4
                        && index + 5 < items.size()
                        && isA6(items.get(index + 4))
                        && isA6(items.get(index + 5))) {

                    List<A4LayoutPlacement> combination =
                            placeFourA7PlusTwoA6(
                                    items.get(index),
                                    items.get(index + 1),
                                    items.get(index + 2),
                                    items.get(index + 3),
                                    items.get(index + 4),
                                    items.get(index + 5),
                                    pageNumber
                            );

                    if (combination != null) {
                        result.addAll(combination);
                        pageNumber++;
                        index += 6;
                        continue;
                    }
                }

                /*
                 * Alle übrigen zusammenhängenden A7.
                 * Auch weniger als 8 werden so kompakt wie
                 * möglich auf einer Seite angeordnet.
                 */
                int countForPage =
                        Math.min(a7Count, 8);

                List<A4LayoutPlacement> a7Block =
                        placeA7Block(
                                items.subList(
                                        index,
                                        index + countForPage
                                ),
                                pageNumber
                        );

                if (a7Block != null) {
                    result.addAll(a7Block);
                    pageNumber++;
                    index += countForPage;
                    continue;
                }
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
             * 2 x A6 + 4 x A7
             * =================================================
             *
             * Zwei A6 hochkant und vier A7 quer.
             *
             * Variante 1:
             *
             *   A6 | A6
             *   ---------
             *   A7 | A7
             *   A7 | A7
             *
             * Variante 2:
             *
             *   A7 | A7
             *   A7 | A7
             *   ---------
             *   A6 | A6
             *
             * Es wird die Variante mit der besseren
             * Mindestskalierung verwendet.
             */
            if (index + 5 < items.size()) {

                A4LayoutItem first = items.get(index);
                A4LayoutItem second = items.get(index + 1);
                A4LayoutItem third = items.get(index + 2);
                A4LayoutItem fourth = items.get(index + 3);
                A4LayoutItem fifth = items.get(index + 4);
                A4LayoutItem sixth = items.get(index + 5);

                if (isA6(first)
                        && isA6(second)
                        && isA7(third)
                        && isA7(fourth)
                        && isA7(fifth)
                        && isA7(sixth)) {

                    List<A4LayoutPlacement> combination =
                            placeTwoA6PlusFourA7(
                                    first,
                                    second,
                                    third,
                                    fourth,
                                    fifth,
                                    sixth,
                                    pageNumber
                            );

                    if (combination != null) {

                        result.addAll(combination);

                        pageNumber++;
                        index += 6;

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
             * Für unbekannte Formate bleibt die bisherige
             * Paarlogik erhalten. A7 wird weiter oben explizit
             * im 2 x 4 Raster verarbeitet.
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

    private boolean isA5A7A7(
            A4LayoutItem first,
            A4LayoutItem second,
            A4LayoutItem third
    ) {

        int a5Count = 0;
        int a7Count = 0;

        if (isA5(first)) {
            a5Count++;
        }

        if (isA5(second)) {
            a5Count++;
        }

        if (isA5(third)) {
            a5Count++;
        }

        if (isA7(first)) {
            a7Count++;
        }

        if (isA7(second)) {
            a7Count++;
        }

        if (isA7(third)) {
            a7Count++;
        }

        return a5Count == 1
                && a7Count == 2;
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

        if (a5Scale < getMinimumScale(a5)) {
            return null;
        }

        float a5Height =
                a5.height() * a5Scale;

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
                    createCandidateInArea(
                            a7Items.get(i),
                            pageNumber,
                            left + i
                                    * (a7CellWidth + GAP),
                            bottom
                                    + a5Height
                                    + GAP,
                            a7CellWidth,
                            a7AreaHeight,
                            false
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


    /*
     * =========================================================
     * 4 x A7 + 2 x A6
     * =========================================================
     */
    private List<A4LayoutPlacement> placeFourA7PlusTwoA6(
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

        /*
         * Die A6 bekommen den unteren Bereich. Die A7 werden
         * in den verbleibenden oberen Bereich eingepasst.
         */
        float a6AreaHeight =
                contentHeight * 0.60f;

        float a7AreaHeight =
                contentHeight
                        - GAP
                        - a6AreaHeight;

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
                    createCandidateInArea(
                            a7Items.get(i),
                            pageNumber,
                            left + i
                                    * (a7CellWidth + GAP),
                            bottom
                                    + a6AreaHeight
                                    + GAP,
                            a7CellWidth,
                            a7AreaHeight,
                            false
                    );

            if (candidate == null) {
                return null;
            }

            result.add(candidate.placement());
        }

        float a6CellWidth =
                (contentWidth - GAP) / 2f;

        Candidate a6First =
                createPortraitCandidate(
                        fifth,
                        pageNumber,
                        left,
                        bottom,
                        a6CellWidth,
                        a6AreaHeight
                );

        Candidate a6Second =
                createPortraitCandidate(
                        sixth,
                        pageNumber,
                        left
                                + a6CellWidth
                                + GAP,
                        bottom,
                        a6CellWidth,
                        a6AreaHeight
                );

        if (a6First == null
                || a6Second == null) {
            return null;
        }

        result.add(a6First.placement());
        result.add(a6Second.placement());

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

    private LayoutCandidate createFourA6Layout(
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

        float cellWidth =
                (contentWidth - GAP) / 2f;

        float cellHeight =
                (contentHeight - GAP) / 2f;


        List<LayoutSlot> slots =
                List.of(
                        new LayoutSlot(
                                left,
                                bottom + cellHeight + GAP,
                                cellWidth,
                                cellHeight,
                                LayoutOrientation.PORTRAIT
                        ),

                        new LayoutSlot(
                                left + cellWidth + GAP,
                                bottom + cellHeight + GAP,
                                cellWidth,
                                cellHeight,
                                LayoutOrientation.PORTRAIT
                        ),

                        new LayoutSlot(
                                left,
                                bottom,
                                cellWidth,
                                cellHeight,
                                LayoutOrientation.PORTRAIT
                        ),

                        new LayoutSlot(
                                left + cellWidth + GAP,
                                bottom,
                                cellWidth,
                                cellHeight,
                                LayoutOrientation.PORTRAIT
                        )
                );

        return createLayoutCandidate(
                List.of(
                        first,
                        second,
                        third,
                        fourth
                ),
                slots,
                pageNumber
        );
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

    private boolean isA6A7A7(
            A4LayoutItem first,
            A4LayoutItem second,
            A4LayoutItem third
    ) {

        int a6Count = 0;
        int a7Count = 0;

        if (isA6(first)) {
            a6Count++;
        }

        if (isA6(second)) {
            a6Count++;
        }

        if (isA6(third)) {
            a6Count++;
        }

        if (isA7(first)) {
            a7Count++;
        }

        if (isA7(second)) {
            a7Count++;
        }

        if (isA7(third)) {
            a7Count++;
        }

        return a6Count == 1
                && a7Count == 2;
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
}