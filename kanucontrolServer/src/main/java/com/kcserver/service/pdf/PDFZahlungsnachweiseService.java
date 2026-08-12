package com.kcserver.service.pdf;

import com.kcserver.entity.Veranstaltung;
import com.kcserver.entity.Zahlungsnachweis;
import com.kcserver.entity.ZahlungsnachweisDokument;
import com.kcserver.enumtype.PdfDokumentTyp;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.repository.abrechnung.ZahlungsnachweisRepository;
import com.kcserver.util.PdfFilenameUtil;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PDFZahlungsnachweiseService {

    private static final PDType1Font FONT =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA
            );

    private static final PDType1Font FONT_BOLD =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_BOLD
            );

    private static final float TITLE_SIZE = 18f;
    private static final float SECTION_SIZE = 11f;
    private static final float TEXT_SIZE = 9f;

    private static final float ROW_HEIGHT = 20f;
    private static final float MAX_DOCUMENT_WIDTH = 400f;
    private static final float MAX_DOCUMENT_HEIGHT = 300f;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final NumberFormat MONEY =
            NumberFormat.getCurrencyInstance(
                    Locale.GERMANY
            );

    record PlatzierteDokumentZuordnung(
            A4LayoutPlacement placement,
            DokumentZuordnung dokumentZuordnung
    ) {
    }

    private final VeranstaltungRepository veranstaltungRepository;
    private final ZahlungsnachweisRepository zahlungsnachweisRepository;
    private final PDFLayoutService layoutService;

    private final A4LayoutEngine layoutEngine;
    private final PDFDocumentComposer composer;

    String createBelegkopf(
            PDFBelegGruppe gruppe
    ) {

        Zahlungsnachweis nachweis =
                gruppe.nachweis();

        return String.format(
                "#%02d  Zahlungsnachweis %s  %s",
                gruppe.nummer(),
                formatDate(nachweis.getDatum()),
                formatMoney(nachweis.getBetrag())
        );
    }

    List<PlatzierteDokumentZuordnung> createPlatzierteDokumentZuordnungen(
            List<A4LayoutPlacement> placements,
            List<DokumentZuordnung> zuordnungen
    ) {

        List<PlatzierteDokumentZuordnung> result =
                new java.util.ArrayList<>();

        for (A4LayoutPlacement placement : placements) {

            DokumentZuordnung zuordnung =
                    findeDokumentZuordnung(
                            placement,
                            zuordnungen
                    );

            result.add(
                    new PlatzierteDokumentZuordnung(
                            placement,
                            zuordnung
                    )
            );
        }

        return result;
    }

    List<PDFBelegGruppe> createGruppen(
            List<Zahlungsnachweis> nachweise
    ) throws IOException {

        List<PDFBelegGruppe> gruppen =
                new java.util.ArrayList<>();

        int nummer = 1;

        for (Zahlungsnachweis nachweis : nachweise) {

            List<A4LayoutItem> dokumente =
                    new java.util.ArrayList<>();

            for (ZahlungsnachweisDokument dokument :
                    nachweis.getDokumente()) {

                String itemId =
                        "ZN-"
                                + nachweis.getId()
                                + "-DOC-"
                                + dokument.getId();

                float[] size;

                try {

                    size =
                            determinePdfSize(
                                    dokument.getInhalt()
                            );

                } catch (IOException e) {

                    throw new IOException(
                            "Dokument ist kein gültiges PDF: "
                                    + "Zahlungsnachweis "
                                    + nachweis.getId()
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
                                size[0],
                                size[1]
                        )
                );
            }

            gruppen.add(
                    new PDFBelegGruppe(
                            nummer,
                            nachweis,
                            dokumente
                    )
            );

            nummer++;
        }

        return gruppen;
    }

    /**
     * Erzeugt das PDF der Zahlungsnachweise.
     *
     * Aktuell enthalten:
     *
     * - Deckblatt
     * - Liste aller Zahlungsnachweise
     * - fortlaufende Belegnummer #01, #02, ...
     * - Gesamtsumme
     * - Logo und Seitennummer
     *
     * Die eigentlichen Zahlungsnachweisdokumente
     * werden in einem späteren Schritt ergänzt.
     */
    public byte[] generate(
            Long veranstaltungId
    ) {

        Veranstaltung veranstaltung =
                veranstaltungRepository
                        .findById(veranstaltungId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Veranstaltung nicht gefunden."
                                )
                        );

        List<Zahlungsnachweis> nachweise =
                zahlungsnachweisRepository
                        .findByVeranstaltungIdOrderByDatumDescIdDesc(
                                veranstaltungId
                        )
                        .stream()
                        .filter(this::hatDokumente)
                        .toList();

        try (
                PDDocument deckblatt =
                        new PDDocument()
        ) {

            /*
             * =====================================================
             * 1. BELEGGRUPPEN ERZEUGEN
             * =====================================================
             *
             * Jeder Zahlungsnachweis bildet eine Beleggruppe.
             *
             * Die Gruppe enthält:
             *
             * - fortlaufende Belegnummer
             * - Zahlungsnachweis
             * - zugehörige Dokumente
             */
            List<PDFBelegGruppe> gruppen =
                    createGruppen(
                            nachweise
                    );

            /*
             * =====================================================
             * 2. DECKBLATT
             * =====================================================
             *
             * Das Deckblatt verwendet dieselben
             * Zahlungsnachweise wie die Beleggruppen.
             */
            createDeckblatt(
                    deckblatt,
                    veranstaltung,
                    nachweise
            );

            /*
             * =====================================================
             * 3. DOKUMENTE SAMMELN
             * =====================================================
             *
             * Key = eindeutige itemId
             *
             * Beispiel:
             *
             * ZN-101-DOC-1001
             */
            Map<String, byte[]> documents =
                    collectDocuments(
                            gruppen
                    );

            /*
             * =====================================================
             * 4. LAYOUT-ITEMS ERZEUGEN
             * =====================================================
             *
             * Die Layout-Items enthalten die
             * Originalgröße der Dokumente.
             */
            List<A4LayoutItem> items =
                    createLayoutItems(
                            gruppen
                    );

            /*
             * =====================================================
             * 5. A4-LAYOUT PLANEN
             * =====================================================
             */
            List<A4LayoutPlacement> placements =
                    layoutEngine.layout(
                            items
                    );
            Map<String, String> belegNummern =
                    createBelegNummern(
                            gruppen
                    );

            /*
             * =====================================================
             * 6. BELEGE AUF A4-SEITEN ZUSAMMENSETZEN
             * =====================================================
             *
             * Noch ohne Footer.
             */
            byte[] belegPdf =
                    composer.composeWithoutFooter(
                            documents,
                            placements,
                            belegNummern
                    );

            /*
             * =====================================================
             * 7. DECKBLATT + BELEGE ZUSAMMENFÜHREN
             * =====================================================
             */
            byte[] gesamtesPdf =
                    mergeDocuments(
                            deckblatt,
                            belegPdf
                    );

            /*
             * =====================================================
             * 8. FOOTER + PDF-METADATEN
             * =====================================================
             *
             * Der Footer wird bewusst erst auf das
             * fertige Gesamtdokument gesetzt.
             */
            String filename =
                    PdfFilenameUtil.build(
                            LocalDate.now(),
                            PdfDokumentTyp.ZAHLUNGSNACHWEISE,
                            veranstaltung
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
                    "Zahlungsnachweise-PDF konnte nicht erzeugt werden.",
                    e
            );
        }
    }

    private Map<String, String> createBelegNummern(
            List<PDFBelegGruppe> gruppen
    ) {

        Map<String, String> result =
                new java.util.LinkedHashMap<>();

        for (PDFBelegGruppe gruppe : gruppen) {

            String belegNummer =
                    String.format(
                            "#%02d",
                            gruppe.nummer()
                    );

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

    private boolean hatDokumente(
            Zahlungsnachweis nachweis
    ) {

        return nachweis.getDokumente() != null
                && !nachweis.getDokumente().isEmpty();
    }

    /*
     * =========================================================
     * DECKBLATT
     * =========================================================
     */

    private void createDeckblatt(
            PDDocument document,
            Veranstaltung veranstaltung,
            List<Zahlungsnachweis> nachweise
    ) throws Exception {

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
                    "Zahlungsnachweise",
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
                            + safe(veranstaltung.getName()),
                    page.getLeft(),
                    FONT_BOLD,
                    SECTION_SIZE
            );

            page.moveY(-18f);

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

            page.moveY(-17f);

            if (veranstaltung.getOrt() != null
                    && !veranstaltung.getOrt().isBlank()) {

                page.write(
                        "Ort: "
                                + veranstaltung.getOrt(),
                        page.getLeft(),
                        FONT,
                        TEXT_SIZE
                );

                page.moveY(-25f);

            } else {

                page.moveY(-15f);
            }

            /*
             * -------------------------------------------------
             * TABELLENPARAMETER
             * -------------------------------------------------
             */

            float tableX =
                    page.getLeft();

            float tableWidth =
                    page.getContentWidth();

            float colBeleg = 45f;
            float colDatum = 70f;
            float colBetrag = 75f;
            float colDokumente = 55f;

            float colBemerkung =
                    tableWidth
                            - colBeleg
                            - colDatum
                            - colDokumente
                            - colBetrag;

            /*
             * -------------------------------------------------
             * TABELLENKOPF
             * -------------------------------------------------
             */

            drawTableHeader(
                    page,
                    tableX,
                    colBeleg,
                    colDatum,
                    colDokumente,
                    colBemerkung,
                    colBetrag
            );

            page.moveY(-ROW_HEIGHT);

            BigDecimal gesamt =
                    BigDecimal.ZERO;

            int nummer = 1;

            /*
             * -------------------------------------------------
             * ZAHLUNGSNACHWEISE
             * -------------------------------------------------
             */

            for (Zahlungsnachweis nachweis : nachweise) {

                /*
                 * Eine Tabellenzeile benötigt ROW_HEIGHT.
                 */
                boolean neueSeite =
                        page.ensureSpace(
                                ROW_HEIGHT
                        );

                /*
                 * Nach einem Seitenwechsel
                 * muss der Tabellenkopf erneut
                 * ausgegeben werden.
                 */
                if (neueSeite) {

                    drawTableHeader(
                            page,
                            tableX,
                            colBeleg,
                            colDatum,
                            colDokumente,
                            colBemerkung,
                            colBetrag
                    );

                    page.moveY(-ROW_HEIGHT);
                }

                float y =
                        page.getY();

                String nummerText =
                        String.format(
                                "#%02d",
                                nummer
                        );

                page.write(
                        nummerText,
                        tableX + 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                page.write(
                        formatDate(
                                nachweis.getDatum()
                        ),
                        tableX
                                + colBeleg
                                + 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                page.write(
                        String.valueOf(
                                nachweis.getDokumente().size()
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
                        truncate(
                                safe(
                                        nachweis.getBemerkung()
                                ),
                                55
                        ),
                        tableX
                                + colBeleg
                                + colDatum
                                + colDokumente
                                + 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                page.writeRight(
                        formatMoney(
                                nachweis.getBetrag()
                        ),
                        tableX + tableWidth - 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                page.line(
                        tableX,
                        y - ROW_HEIGHT,
                        tableX + tableWidth,
                        y - ROW_HEIGHT
                );

                if (nachweis.getBetrag() != null) {

                    gesamt =
                            gesamt.add(
                                    nachweis.getBetrag()
                            );
                }

                nummer++;

                page.moveY(-ROW_HEIGHT);
            }

            /*
             * -------------------------------------------------
             * GESAMTSUMME
             * -------------------------------------------------
             */

            page.ensureSpace(30f);

            page.moveY(-15f);

            page.writeRight(
                    "Gesamt: "
                            + formatMoney(gesamt),
                    tableX + tableWidth,
                    FONT_BOLD,
                    SECTION_SIZE
            );
        }
    }

    /*
     * =========================================================
     * TABELLE
     * =========================================================
     */

    private void drawTableHeader(
            PDFPageWriter page,
            float x,
            float colBeleg,
            float colDatum,
            float colDokumente,
            float colBemerkung,
            float colBetrag
    ) throws Exception {

        float y =
                page.getY();

        page.write(
                "Beleg",
                x + 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.write(
                "Datum",
                x + colBeleg + 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.write(
                "Dokumente",
                x
                        + colBeleg
                        + colDatum
                        + 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.write(
                "Bemerkung",
                x
                        + colBeleg
                        + colDatum
                        + colDokumente
                        + 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.writeRight(
                "Betrag",
                x
                        + colBeleg
                        + colDatum
                        + colBemerkung
                        + colBetrag
                        - 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.line(
                x,
                y - ROW_HEIGHT,
                x
                        + colBeleg
                        + colDatum
                        + colBemerkung
                        + colBetrag,
                y - ROW_HEIGHT
        );
    }

    /*
     * =========================================================
     * FORMATIERUNG
     * =========================================================
     */

    private String formatDate(
            LocalDate date
    ) {

        return date == null
                ? ""
                : date.format(DATE_FORMAT);
    }

    private String formatMoney(
            BigDecimal value
    ) {

        return MONEY.format(
                value == null
                        ? BigDecimal.ZERO
                        : value
        );
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

    private Map<String, byte[]> collectDocuments(
            List<PDFBelegGruppe> gruppen
    ) {

        Map<String, byte[]> documents =
                new java.util.LinkedHashMap<>();

        for (PDFBelegGruppe gruppe : gruppen) {

            for (ZahlungsnachweisDokument dokument :
                    gruppe.nachweis().getDokumente()) {

                String id =
                        "ZN-"
                                + gruppe.nachweis().getId()
                                + "-DOC-"
                                + dokument.getId();

                documents.put(
                        id,
                        dokument.getInhalt()
                );
            }
        }

        return documents;
    }

    private List<A4LayoutItem> createLayoutItems(
            List<PDFBelegGruppe> gruppen
    ) {

        List<A4LayoutItem> items =
                new java.util.ArrayList<>();

        for (PDFBelegGruppe gruppe : gruppen) {

            items.addAll(
                    gruppe.dokumente()
            );
        }

        return items;
    }

    private float[] determinePdfSize(
            byte[] content
    ) throws IOException {

        /*
         * =========================================================
         * PDF
         * =========================================================
         */

        if (content.length >= 5
                && content[0] == '%'
                && content[1] == 'P'
                && content[2] == 'D'
                && content[3] == 'F'
                && content[4] == '-') {

            try (
                    PDDocument document =
                            org.apache.pdfbox.Loader.loadPDF(content)
            ) {

                if (document.getNumberOfPages() == 0) {
                    throw new IllegalArgumentException(
                            "PDF enthält keine Seite."
                    );
                }

                PDRectangle box =
                        document
                                .getPage(0)
                                .getMediaBox();

                /*
                 * PDFs werden mit ihrer tatsächlichen
                 * PDF-Seitengröße an die Layout-Engine
                 * übergeben.
                 *
                 * Die Skalierung erfolgt später durch
                 * die A4LayoutEngine.
                 */
                return new float[]{
                        box.getWidth(),
                        box.getHeight()
                };
            }
        }

        /*
         * =========================================================
         * Bild
         * =========================================================
         */

        BufferedImage image =
                ImageIO.read(
                        new ByteArrayInputStream(content)
                );

        if (image != null) {

            float width = image.getWidth();
            float height = image.getHeight();

            /*
             * Bilder werden auf einen sinnvollen Bereich
             * für das A4-Layout begrenzt.
             *
             * Das Seitenverhältnis bleibt erhalten.
             */
            float scale =
                    Math.min(
                            MAX_DOCUMENT_WIDTH / width,
                            MAX_DOCUMENT_HEIGHT / height
                    );

            /*
             * Kleine Bilder nicht künstlich vergrößern.
             */
            scale = Math.min(scale, 1f);

            return new float[]{
                    width * scale,
                    height * scale
            };
        }

        throw new IOException(
                "Dokument ist weder ein gültiges PDF noch ein "
                        + "unterstütztes Bildformat."
        );
    }
    private byte[] mergeDocuments(
            PDDocument deckblatt,
            byte[] belegPdf
    ) throws IOException {

        /*
         * Keine Belege vorhanden.
         *
         * Dann besteht das Gesamtdokument ausschließlich
         * aus dem Deckblatt.
         */
        if (belegPdf == null || belegPdf.length == 0) {

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
                    new LayerUtility(gesamt);

            /*
             * -------------------------------------------------
             * DECKBLATT
             * -------------------------------------------------
             */

            int deckblattIndex = 0;

            for (PDPage ignored :
                    deckblatt.getPages()) {

                org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject form =
                        layerUtility.importPageAsForm(
                                deckblatt,
                                deckblattIndex
                        );

                PDPage targetPage =
                        new PDPage(
                                PDFLayoutService.PAGE_SIZE
                        );

                gesamt.addPage(targetPage);

                try (
                        PDPageContentStream content =
                                new PDPageContentStream(
                                        gesamt,
                                        targetPage
                                )
                ) {

                    content.drawForm(form);
                }

                deckblattIndex++;
            }

            /*
             * -------------------------------------------------
             * BELEGE
             * -------------------------------------------------
             */

            for (int i = 0;
                 i < belege.getNumberOfPages();
                 i++) {

                org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject form =
                        layerUtility.importPageAsForm(
                                belege,
                                i
                        );

                PDPage targetPage =
                        new PDPage(
                                PDFLayoutService.PAGE_SIZE
                        );

                gesamt.addPage(targetPage);

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

    List<DokumentZuordnung> createDokumentZuordnungen(
            List<PDFBelegGruppe> gruppen
    ) {

        List<DokumentZuordnung> result =
                new java.util.ArrayList<>();

        for (PDFBelegGruppe gruppe : gruppen) {

            for (A4LayoutItem dokument :
                    gruppe.dokumente()) {

                result.add(
                        new DokumentZuordnung(
                                dokument.id(),
                                gruppe,
                                dokument
                        )
                );
            }
        }

        return result;
    }

    DokumentZuordnung findeDokumentZuordnung(
            A4LayoutPlacement placement,
            List<DokumentZuordnung> zuordnungen
    ) {

        return zuordnungen.stream()
                .filter(zuordnung ->
                        zuordnung.itemId()
                                .equals(placement.itemId())
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Keine Dokumentzuordnung für itemId: "
                                        + placement.itemId()
                        )
                );
    }
}