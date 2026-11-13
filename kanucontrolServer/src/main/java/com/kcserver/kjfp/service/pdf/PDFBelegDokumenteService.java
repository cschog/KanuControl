package com.kcserver.kjfp.service.pdf;

import com.kcserver.kjfp.entity.abrechnung.AbrechnungBeleg;
import com.kcserver.kjfp.entity.abrechnung.AbrechnungBuchung;
import com.kcserver.kjfp.entity.Dokument;
import com.kcserver.kjfp.enumtype.BuchungsHerkunft;
import com.kcserver.kjfp.enumtype.PdfDokumentTyp;
import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.kjfp.repository.abrechnung.AbrechnungBelegRepository;
import com.kcserver.core.util.PdfFilenameUtil;
import com.kcserver.core.util.PdfPaperFormatUtil;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PDFBelegDokumenteService {

    private static final PDType1Font FONT =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA
            );

    private static final PDType1Font FONT_BOLD =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_BOLD
            );

    private final PDFDocumentSizeService documentSizeService;

    private static final float TITLE_SIZE = 18f;
    private static final float SECTION_SIZE = 11f;
    private static final float TEXT_SIZE = 9f;

    private static final float ROW_HEIGHT = 20f;

    /**
     * Referenzhöhe für Bilder.
     *
     * Die A4LayoutEngine übernimmt anschließend
     * die tatsächliche Skalierung auf der Seite.
     */

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final AbrechnungBelegRepository belegRepository;
    private final PDFLayoutService layoutService;
    private final A4LayoutEngine layoutEngine;
    private final PDFDocumentComposer composer;


    /*
     * =========================================================
     * GENERATE
     * =========================================================
     */

    @Transactional(readOnly = true)
    public byte[] generate(
            Long veranstaltungId
    ) {

        List<AbrechnungBeleg> belege =
                belegRepository
                        .findByVeranstaltungIdWithDokumente(
                                veranstaltungId
                        )
                        .stream()
                        .filter(this::hasManualBooking)
                        .sorted(java.util.Comparator.comparing(AbrechnungBeleg::getBelegnummer, BelegnummerSortierung.NUMMERN).thenComparing(AbrechnungBeleg::getId))
                        .toList();

        if (belege.isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ErrorMessages.NO_BELEGE_FOR_ABRECHNUNG
            );
        }

        try (
                PDDocument deckblatt =
                        new PDDocument()
        ) {

            /*
             * =================================================
             * 1. BELEGGRUPPEN
             * =================================================
             */

            List<BelegDokumentGruppe> gruppen =
                    createGruppen(
                            belege
                    );

            /*
             * =================================================
             * 2. DECKBLATT
             * =================================================
             */

            createDeckblatt(
                    deckblatt,
                    belege
            );

            /*
             * =================================================
             * 3. DOKUMENTE SAMMELN
             * =================================================
             */

            Map<String, byte[]> documents =
                    collectDocuments(
                            gruppen
                    );

            /*
             * =================================================
             * 4. LAYOUT-ITEMS
             * =================================================
             */

            List<A4LayoutItem> items =
                    createLayoutItems(
                            gruppen
                    );

            /*
             * =================================================
             * 5. A4-LAYOUT
             * =================================================
             */

            List<A4LayoutPlacement> placements =
                    layoutEngine.layout(
                            items
                    );

            /*
             * =================================================
             * 6. BELEGNUMMERN
             * =================================================
             */

            Map<String, String> belegNummern =
                    createBelegNummern(
                            gruppen
                    );

            /*
             * =================================================
             * 7. DOKUMENTE ZUSAMMENSETZEN
             * ================================================= */

            byte[] belegPdf =
                    composer.composeWithoutFooter(
                            documents,
                            placements,
                            belegNummern
                    );

            /*
             * =================================================
             * 8. DECKBLATT + BELEGE
             * =================================================
             */

            byte[] gesamtesPdf =
                    mergeDocuments(
                            deckblatt,
                            belegPdf
                    );

            /*
             * =================================================
             * 9. FOOTER + METADATEN
             * =================================================
             */

            String filename =
                    PdfFilenameUtil.build(
                            LocalDate.now(),
                            PdfDokumentTyp.BELEGE,
                            belege.getFirst()
                                    .getAbrechnung()
                                    .getVeranstaltung()
                    );

            try (
                    PDDocument document =
                            org.apache.pdfbox.Loader.loadPDF(
                                    gesamtesPdf
                            );

                    ByteArrayOutputStream out =
                            new ByteArrayOutputStream()
            ) {

                layoutService.addFooter(
                        document
                );

                document.getDocumentInformation()
                        .setTitle(filename);

                document.getDocumentInformation()
                        .setAuthor("KanuControl");

                document.getDocumentInformation()
                        .setCreator("KanuControl");

                document.save(out);

                return out.toByteArray();
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Belegdokumente-PDF konnte nicht erzeugt werden.",
                    e
            );
        }
    }

    private boolean hasManualBooking(
            AbrechnungBeleg beleg
    ) {

        return beleg.getPositionen()
                .stream()
                .anyMatch(
                        buchung ->
                                buchung.getHerkunft()
                                        == BuchungsHerkunft.MANUELL
                );
    }


    /*
     * =========================================================
     * BELEGGRUPPEN
     * =========================================================
     */

    private List<BelegDokumentGruppe> createGruppen(
            List<AbrechnungBeleg> belege
    ) {

        List<BelegDokumentGruppe> gruppen =
                new ArrayList<>();

        int nummer = 1;

        for (AbrechnungBeleg beleg : belege) {

            List<A4LayoutItem> dokumente =
                    new ArrayList<>();

            for (Dokument dokument :
                    beleg.getDokumente()) {

                String itemId =
                        createItemId(
                                beleg,
                                dokument
                        );


                PDFDocumentSize size;

                try {

                    size =
                            documentSizeService.determine(
                                    dokument
                            );

                } catch (IllegalStateException e) {

                    throw new RuntimeException(
                            "Keine Dokumentgröße hinterlegt: "
                                    + "Beleg "
                                    + beleg.getId()
                                    + ", Dokument "
                                    + dokument.getId()
                                    + ", Dateiname "
                                    + dokument.getOriginalDateiname(),
                            e
                    );
                }

                dokumente.add(
                        new A4LayoutItem(
                                itemId,
                                size.width(),
                                size.height(),
                                size.density(),
                                size.referenzObjekt()
                        )
                );
            }

            gruppen.add(
                    new BelegDokumentGruppe(
                            nummer,
                            beleg,
                            dokumente
                    )
            );

            nummer++;
        }

        return gruppen;
    }


    private String createItemId(
            AbrechnungBeleg beleg,
            Dokument dokument
    ) {
        return "BELEG-"
                + beleg.getId()
                + "-DOC-"
                + dokument.getId();
    }


    private boolean hatDokumente(
            AbrechnungBeleg beleg
    ) {

        return beleg.getDokumente() != null
                && !beleg.getDokumente().isEmpty();
    }


    /*
     * =========================================================
     * BELEGNUMMERN
     * =========================================================
     */

    private Map<String, String> createBelegNummern(
            List<BelegDokumentGruppe> gruppen
    ) {

        Map<String, String> result =
                new LinkedHashMap<>();

        for (BelegDokumentGruppe gruppe : gruppen) {

            String belegNummer =
                    safe(gruppe.beleg().getBelegnummer());

            for (A4LayoutItem dokument :
                    gruppe.dokumente()) {

                result.put(
                        dokument.id(),
                        belegNummer
                );
            }
        }

        return result;
    }


    /*
     * =========================================================
     * DOKUMENTE
     * =========================================================
     */

    private Map<String, byte[]> collectDocuments(
            List<BelegDokumentGruppe> gruppen
    ) {

        Map<String, byte[]> documents =
                new LinkedHashMap<>();

        for (BelegDokumentGruppe gruppe : gruppen) {

            for (Dokument dokument :
                    gruppe.beleg().getDokumente()) {

                documents.put(
                        createItemId(
                                gruppe.beleg(),
                                dokument
                        ),
                        dokument.getInhalt()
                );
            }
        }

        return documents;
    }


    private List<A4LayoutItem> createLayoutItems(
            List<BelegDokumentGruppe> gruppen
    ) {

        List<A4LayoutItem> items =
                new ArrayList<>();

        for (BelegDokumentGruppe gruppe : gruppen) {

            items.addAll(
                    gruppe.dokumente()
            );
        }

        return items;
    }


    /*
     * =========================================================
     * DECKBLATT
     * =========================================================
     */

    private void createDeckblatt(
            PDDocument document,
            List<AbrechnungBeleg> belege
    ) throws Exception {

        AbrechnungBeleg ersterBeleg =
                belege.getFirst();

        var veranstaltung =
                ersterBeleg
                        .getAbrechnung()
                        .getVeranstaltung();

        try (
                PDFPageWriter page =
                        new PDFPageWriter(document)
        ) {

            /*
             * -------------------------------------------------
             * TITEL
             * -------------------------------------------------
             */

            page.write(
                    "Belege",
                    page.getLeft(),
                    FONT_BOLD,
                    TITLE_SIZE
            );

            page.moveY(-35f);

            /*
             * -------------------------------------------------
             * VERANSTALTUNG
             * -------------------------------------------------
             */

            page.write(
                    "Veranstaltung: "
                            + safe(
                            veranstaltung.getName()
                    ),
                    page.getLeft(),
                    FONT_BOLD,
                    SECTION_SIZE
            );

            page.moveY(-18f);

            /*
             * -------------------------------------------------
             * ZEITRAUM
             * -------------------------------------------------
             */

            String zeitraum =
                    formatDate(
                            veranstaltung.getBeginnDatum()
                    )
                            + " - "
                            + formatDate(
                            veranstaltung.getEndeDatum()
                    );

            page.write(
                    "Zeitraum: " + zeitraum,
                    page.getLeft(),
                    FONT,
                    TEXT_SIZE
            );

            page.moveY(-18f);

            /*
             * -------------------------------------------------
             * HINWEIS ORIGINALBELEGE
             * -------------------------------------------------
             */

            page.write(
                    "Nur für die eigene Dokumentation.",
                    page.getLeft(),
                    FONT_BOLD,
                    TEXT_SIZE
            );

            page.moveY(-13f);

            page.write(
                    "Alle Belege müssen weiterhin im Original bei KVNRW eingereicht werden.",
                    page.getLeft(),
                    FONT_BOLD,
                    TEXT_SIZE
            );

            page.moveY(-25f);

            /*
             * -------------------------------------------------
             * TABELLE
             * -------------------------------------------------
             */

            float tableX =
                    page.getLeft();

            float tableWidth =
                    page.getContentWidth();

            float colBeleg = 80f;
            float colDatum = 60f;
            float colDokumente = 40f;
            float colAussteller = 75f;
            float colBetrag = 140f;

            float colBeschreibung =
                    tableWidth
                            - colBeleg
                            - colDatum
                            - colDokumente
                            - colAussteller
                            - colBetrag;

            drawTableHeader(
                    page,
                    tableX,
                    colBeleg,
                    colDatum,
                    colDokumente,
                    colAussteller,
                    colBetrag,
                    colBeschreibung
            );

            page.moveY(-ROW_HEIGHT);

            BigDecimal kontoEinnahmen = BigDecimal.ZERO;
            BigDecimal kontoAusgaben = BigDecimal.ZERO;
            BigDecimal gesamtEinnahmen = BigDecimal.ZERO;
            BigDecimal gesamtAusgaben = BigDecimal.ZERO;
            for (int index = 0; index < belege.size(); index++) {
                AbrechnungBeleg beleg = belege.get(index);
                boolean kontoEnde = index + 1 == belege.size()
                        || !kontoSchluessel(beleg).equals(kontoSchluessel(belege.get(index + 1)));

                boolean neueSeite =
                        page.ensureSpace(
                                kontoEnde ? 2 * ROW_HEIGHT : ROW_HEIGHT
                        );

                if (neueSeite) {

                    drawTableHeader(
                            page,
                            tableX,
                            colBeleg,
                            colDatum,
                            colDokumente,
                                    colAussteller,
                            colBetrag,
                            colBeschreibung
                    );

                    page.moveY(-ROW_HEIGHT);
                }

                float y =
                        page.getY();

                page.write(
                        safe(beleg.getBelegnummer()),
                        tableX + 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                page.write(
                        formatDate(
                                beleg.getDatum()
                        ),
                        tableX
                                + colBeleg
                                + 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                page.write(
                        formatDokumente(
                                beleg.getDokumente()
                        ),
                        tableX
                                + colBeleg
                                + colDatum
                                + 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                page.write(
                        fitText(safe(beleg.getAussteller()), colAussteller - 6, FONT, TEXT_SIZE),
                        tableX
                                + colBeleg
                                + colDatum
                                + colDokumente
                                + 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );
                // BESCHREIBUNG
                page.write(
                        fitText(safe(beleg.getBeschreibung()), colBeschreibung - 6, FONT, TEXT_SIZE),
                        tableX
                                + colBeleg
                                + colDatum
                                + colDokumente
                                + colAussteller
                                + 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                BigDecimal einnahmen = getBelegEinnahmen(beleg);
                BigDecimal ausgaben = getBelegAusgaben(beleg);
                writeBetraege(page, einnahmen, ausgaben, tableX + tableWidth, y - 14, false);
                kontoEinnahmen = kontoEinnahmen.add(einnahmen);
                kontoAusgaben = kontoAusgaben.add(ausgaben);
                gesamtEinnahmen = gesamtEinnahmen.add(einnahmen);
                gesamtAusgaben = gesamtAusgaben.add(ausgaben);
                page.moveY(-ROW_HEIGHT);
                if (kontoEnde) {
                    float sumY = page.getY();
                    page.line(tableX, sumY, tableX + tableWidth, sumY);
                    page.write("Zwischensumme " + kontoBezeichnung(beleg), tableX + 3,
                            sumY - 14, FONT_BOLD, TEXT_SIZE);
                    writeBetraege(page, kontoEinnahmen, kontoAusgaben,
                            tableX + tableWidth, sumY - 14, true);
                    page.moveY(-ROW_HEIGHT);
                    kontoEinnahmen = BigDecimal.ZERO;
                    kontoAusgaben = BigDecimal.ZERO;
                }
            }

            page.ensureSpace(2 * ROW_HEIGHT);
            page.write("Gesamtsumme", tableX + 3, page.getY() - 14, FONT_BOLD, TEXT_SIZE);
            writeBetraege(page, gesamtEinnahmen, gesamtAusgaben,
                    tableX + tableWidth, page.getY() - 14, true);
            page.moveY(-ROW_HEIGHT);

            page.ensureSpace(30f);

            page.moveY(-15f);

            page.writeRight(
                    "Anzahl Belege: "
                            + belege.size(),
                    tableX + tableWidth,
                    FONT_BOLD,
                    SECTION_SIZE
            );
            /*
             * -------------------------------------------------
             * HINWEIS FEHLENDE BELEGDOKUMENTE
             * -------------------------------------------------
             */

            long fehlendeDokumente =
                    belege.stream()
                            .filter(beleg -> !hatDokumente(beleg))
                            .count();

            if (fehlendeDokumente > 0) {

                page.ensureSpace(30f);

                page.moveY(-15f);

                String hinweis;

                if (fehlendeDokumente == 1) {

                    hinweis =
                            "Hinweis: Für einen Beleg "
                                    + "ist noch kein Nachweisdokument vorhanden.";

                } else {

                    hinweis =
                            "Hinweis: Für "
                                    + fehlendeDokumente
                                    + " Belege sind noch keine "
                                    + "Nachweisdokumente vorhanden.";
                }

                page.write(
                        hinweis,
                        tableX,
                        page.getY(),
                        FONT_BOLD,
                        TEXT_SIZE
                );
            }
        }
    }


    private void drawTableHeader(
            PDFPageWriter page,
            float x,
            float colBeleg,
            float colDatum,
            float colDokumente,
            float colAussteller,
            float colBetrag,
            float colBeschreibung
    ) throws Exception {

        float xDatum = x + colBeleg;
        float xDokumente = xDatum + colDatum;
        float xAussteller = xDokumente + colDokumente;
        float xBeschreibung = xAussteller + colAussteller;
        float xBetrag = xBeschreibung + colBeschreibung;
        float tableRight = xBetrag + colBetrag;

        float y =
                page.getY();

        page.write(
                "Belegnummer",
                x + 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.write(
                "Datum",
                xDatum + 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.write(
                "#Docs",
                xDokumente + 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.write(
                "Aussteller",
                xAussteller + 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.write(
                "Beschreibung",
                xBeschreibung + 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.writeRight("Einnahmen", tableRight - 70, y - 14, FONT_BOLD, TEXT_SIZE);
        page.writeRight(
                "Ausgaben",
                tableRight,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.line(
                x,
                y - ROW_HEIGHT,
                tableRight,
                y - ROW_HEIGHT
        );
    }

    /*
     * =========================================================
     * MERGE
     * =========================================================
     */

    private byte[] mergeDocuments(
            PDDocument deckblatt,
            byte[] belegPdf
    ) throws IOException {

        if (belegPdf == null
                || belegPdf.length == 0) {

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            deckblatt.save(out);

            return out.toByteArray();
        }

        try (
                PDDocument belege =
                        org.apache.pdfbox.Loader.loadPDF(
                                belegPdf
                        );

                PDDocument gesamt =
                        new PDDocument();

                ByteArrayOutputStream out =
                        new ByteArrayOutputStream()
        ) {

            LayerUtility layerUtility =
                    new LayerUtility(
                            gesamt
                    );

            /*
             * Deckblatt
             */

            for (int i = 0;
                 i < deckblatt.getNumberOfPages();
                 i++) {

                var form =
                        layerUtility.importPageAsForm(
                                deckblatt,
                                i
                        );

                PDPage targetPage =
                        new PDPage(
                                PDFLayoutService.PAGE_SIZE
                        );

                gesamt.addPage(
                        targetPage
                );

                try (
                        PDPageContentStream content =
                                new PDPageContentStream(
                                        gesamt,
                                        targetPage
                                )
                ) {

                    content.drawForm(form);
                }
            }

            /*
             * Belegdokumente
             */

            for (int i = 0;
                 i < belege.getNumberOfPages();
                 i++) {

                var form =
                        layerUtility.importPageAsForm(
                                belege,
                                i
                        );

                PDPage targetPage =
                        new PDPage(
                                PDFLayoutService.PAGE_SIZE
                        );

                gesamt.addPage(
                        targetPage
                );

                try (
                        PDPageContentStream content =
                                new PDPageContentStream(
                                        gesamt,
                                        targetPage
                                )
                ) {

                    content.drawForm(form);
                }
            }

            gesamt.save(out);

            return out.toByteArray();
        }
    }


    /*
     * =========================================================
     * HILFSMETHODEN
     * =========================================================
     */

    private String formatDate(
            LocalDate date
    ) {

        return date == null
                ? ""
                : date.format(DATE_FORMAT);
    }


    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }


    private String truncate(
            String value,
            int maxLength
    ) {

        if (value == null) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(
                0,
                maxLength - 1
        ) + "…";
    }

    private String formatBetrag(
            java.math.BigDecimal betrag
    ) {

        if (betrag == null) {
            return "";
        }

        return String.format(
                java.util.Locale.GERMANY,
                "%,.2f €",
                betrag
        );
    }
    private String fitText(String text, float width, org.apache.pdfbox.pdmodel.font.PDFont font,
                           float size) throws IOException {
        if (font.getStringWidth(text) / 1000 * size <= width) return text;
        while (!text.isEmpty() && font.getStringWidth(text + "...") / 1000 * size > width) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "...";
    }

    private void writeBetraege(PDFPageWriter page, BigDecimal einnahmen, BigDecimal ausgaben,
                              float right, float y, boolean bold) throws IOException {
        var font = bold ? FONT_BOLD : FONT;
        if (einnahmen.setScale(2, java.math.RoundingMode.HALF_UP).signum() != 0) {
            page.writeRight(formatBetrag(einnahmen), right - 70, y, font, TEXT_SIZE);
        }
        if (ausgaben.setScale(2, java.math.RoundingMode.HALF_UP).signum() != 0) {
            page.writeRight(formatBetrag(ausgaben), right, y, font, TEXT_SIZE);
        }
    }

    private BigDecimal getBelegEinnahmen(AbrechnungBeleg beleg) {
        return beleg.getPositionen().stream()
                .filter(p -> p.getKategorie() != null && p.getKategorie().isEinnahme())
                .map(AbrechnungBuchung::getBetrag).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getBelegAusgaben(AbrechnungBeleg beleg) {
        return beleg.getPositionen().stream()
                .filter(p -> p.getKategorie() == null || p.getKategorie().isKosten())
                .map(AbrechnungBuchung::getBetrag).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String kontoSchluessel(AbrechnungBeleg beleg) {
        var konto = beleg.getFinanzGruppe();
        return konto != null && konto.getId() != null
                ? konto.getId().toString() : kontoBezeichnung(beleg);
    }

    private String kontoBezeichnung(AbrechnungBeleg beleg) {
        if (beleg.getFinanzGruppe() != null) return safe(beleg.getFinanzGruppe().getKuerzel());
        String nummer = safe(beleg.getBelegnummer());
        int separator = nummer.lastIndexOf('_');
        return separator < 0 ? nummer : nummer.substring(0, separator);
    }

    private BigDecimal getBelegBetrag(AbrechnungBeleg beleg) {
        return beleg.getPositionen()
                .stream()
                .map(AbrechnungBuchung::getBetrag)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String formatDokumente(
            List<Dokument> dokumente
    ) {

        if (dokumente == null || dokumente.isEmpty()) {
            return "0";
        }

        String formate =
                dokumente.stream()
                        .map(PdfPaperFormatUtil::referenceFormat)
                        .collect(
                                java.util.stream.Collectors.joining(", ")
                        );

        return dokumente.size()
                + " ("
                + formate
                + ")";
    }
}