package com.kcserver.service.pdf;

import com.kcserver.enumtype.PdfDocumentDensity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Plant Dokumente auf DIN-A4-Seiten.
 *
 * Die Engine kennt ausschließlich Dokumentgrößen
 * und die durch die Bildanalyse ermittelte Density.
 *
 * Grundregeln:
 *
 * - HIGH-Dokumente erhalten grundsätzlich eine eigene Seite.
 * - MEDIUM-Dokumente werden bevorzugt paarweise platziert.
 * - LOW-Dokumente werden bevorzugt paarweise platziert.
 * - Eine gemeinsame Platzierung ist nur zulässig,
 *   wenn die Mindestskalierung beider Dokumente erreicht wird.
 * - Dokumente werden niemals größer als ihre Originalgröße.
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
     * Mindestskalierung abhängig von der Density.
     *
     * LOW:
     * Wenig Inhalt -> darf kleiner dargestellt werden.
     *
     * MEDIUM:
     * Normale Informationsdichte.
     *
     * HIGH:
     * Viel Inhalt -> möglichst groß darstellen.
     */
    private static final float MIN_SCALE_LOW = 0.40f;
    private static final float MIN_SCALE_MEDIUM = 0.55f;
    private static final float MIN_SCALE_HIGH = 0.70f;

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
        int index = 0;

        while (index < items.size()) {

            A4LayoutItem current =
                    items.get(index);

            /*
             * HIGH bekommt grundsätzlich eine eigene Seite.
             */
            if (current.density() == PdfDocumentDensity.HIGH) {

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

                continue;
            }

            /*
             * Versuchen, das aktuelle Dokument
             * gemeinsam mit dem nächsten Dokument
             * auf einer Seite zu platzieren.
             */
            if (index + 1 < items.size()) {

                A4LayoutItem next =
                        items.get(index + 1);

                /*
                 * HIGH soll nicht mit einem anderen
                 * Dokument kombiniert werden.
                 */
                if (next.density() != PdfDocumentDensity.HIGH) {

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
             * Paarweise Platzierung nicht möglich:
             * Dokument bekommt eine eigene Seite.
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
     * ---------------------------------------------------------
     * EINZELNES DOKUMENT
     * ---------------------------------------------------------
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
                item.rotationAllowed()
                        ? createCandidate(
                        item,
                        pageNumber,
                        contentWidth,
                        contentHeight,
                        true
                )
                        : null;

        Candidate selected =
                selectBestCandidate(
                        normal,
                        rotated
                );

        /*
         * Das Dokument passt nicht einmal
         * auf eine A4-Seite.
         */
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
     * ---------------------------------------------------------
     * PAAR
     * ---------------------------------------------------------
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
         * Variante 1:
         *
         * zwei Dokumente untereinander
         */
        float cellHeight =
                (
                        contentHeight - GAP
                ) / 2f;

        CandidatePair vertical =
                createPairCandidate(
                        first,
                        second,
                        pageNumber,
                        0,
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
         * Variante 2:
         *
         * zwei Dokumente nebeneinander
         */
        float cellWidth =
                (
                        contentWidth - GAP
                ) / 2f;

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
                first.rotationAllowed()
                        ? createCandidateInArea(
                        first,
                        pageNumber,
                        firstX,
                        firstY,
                        firstWidth,
                        firstHeight,
                        true
                )
                        : null;

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
                second.rotationAllowed()
                        ? createCandidateInArea(
                        second,
                        pageNumber,
                        secondX,
                        secondY,
                        secondWidth,
                        secondHeight,
                        true
                )
                        : null;

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
     * ---------------------------------------------------------
     * CANDIDATE
     * ---------------------------------------------------------
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

        /*
         * Density bestimmt,
         * wie klein das Dokument werden darf.
         */
        if (scale < getMinimumScale(item)) {
            return null;
        }

        float width =
                sourceWidth * scale;

        float height =
                sourceHeight * scale;

        /*
         * Zentrieren.
         */
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
     * ---------------------------------------------------------
     * AUSWAHL
     * ---------------------------------------------------------
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
     * ---------------------------------------------------------
     * MINDESTSKALIERUNG
     * ---------------------------------------------------------
     */

    private float getMinimumScale(
            A4LayoutItem item
    ) {

        return switch (item.density()) {

            case LOW ->
                    MIN_SCALE_LOW;

            case MEDIUM ->
                    MIN_SCALE_MEDIUM;

            case HIGH ->
                    MIN_SCALE_HIGH;
        };
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
     * ---------------------------------------------------------
     * INTERN
     * ---------------------------------------------------------
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