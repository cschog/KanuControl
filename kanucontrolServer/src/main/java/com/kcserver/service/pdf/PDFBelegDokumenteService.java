package com.kcserver.service.pdf;

import com.kcserver.entity.AbrechnungBeleg;
import com.kcserver.entity.AbrechnungBuchung;
import com.kcserver.entity.BelegDokument;
import com.kcserver.enumtype.PdfDocumentDensity;
import com.kcserver.enumtype.PdfDokumentTyp;
import com.kcserver.repository.abrechnung.AbrechnungBelegRepository;
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
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
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

    private final ImageAnalysisService imageAnalysisService;

    private static final PDType1Font FONT =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA
            );

    private record DocumentSize(
            float width,
            float height,
            PdfDocumentDensity density
    ) {
    }

    private static final PDType1Font FONT_BOLD =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_BOLD
            );

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
    private static final float IMAGE_REFERENCE_HEIGHT = 700f;

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
                        .filter(this::hatDokumente)
                        .toList();

        if (belege.isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Für die Abrechnung sind keine Belegdokumente vorhanden."
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

            for (BelegDokument dokument :
                    beleg.getDokumente()) {

                String itemId =
                        createItemId(
                                beleg,
                                dokument
                        );

                DocumentSize size;

                try {

                    size =
                            determineDocumentSize(
                                    dokument.getInhalt()
                            );

                } catch (IOException e) {

                    throw new RuntimeException(
                            "Dokument ist kein gültiges PDF/Bild: "
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
                                false
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
            BelegDokument dokument
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

            for (BelegDokument dokument :
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

            float colBeleg = 45f;
            float colDatum = 70f;
            float colDokumente = 55f;
            float colBelegnummer = 90f;
            float colAussteller = 90f;
            float colBetrag = 75f;

            float colBeschreibung =
                    tableWidth
                            - colBeleg
                            - colDatum
                            - colDokumente
                            - colBelegnummer
                            - colAussteller
                            - colBetrag;

            drawTableHeader(
                    page,
                    tableX,
                    colBeleg,
                    colDatum,
                    colDokumente,
                    colBelegnummer,
                    colAussteller,
                    colBetrag,
                    colBeschreibung
            );

            page.moveY(-ROW_HEIGHT);

            int nummer = 1;

            for (AbrechnungBeleg beleg : belege) {

                boolean neueSeite =
                        page.ensureSpace(
                                ROW_HEIGHT
                        );

                if (neueSeite) {

                    drawTableHeader(
                            page,
                            tableX,
                            colBeleg,
                            colDatum,
                            colDokumente,
                            colBelegnummer,
                            colAussteller,
                            colBetrag,
                            colBeschreibung
                    );

                    page.moveY(-ROW_HEIGHT);
                }

                float y =
                        page.getY();

                page.write(
                        String.format(
                                "#%02d",
                                nummer
                        ),
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
                        String.valueOf(
                                beleg.getDokumente().size()
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
                                        beleg.getBelegnummer()
                                ),
                                18
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

                page.write(
                        truncate(
                                safe(
                                        beleg.getAussteller()
                                ),
                                20
                        ),
                        tableX
                                + colBeleg
                                + colDatum
                                + colDokumente
                                + colBelegnummer
                                + 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );
                // BESCHREIBUNG
                page.write(
                        truncate(
                                safe(beleg.getBeschreibung()),
                                45
                        ),
                        tableX
                                + colBeleg
                                + colDatum
                                + colDokumente
                                + colBelegnummer
                                + colAussteller
                                + 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                // BETRAG
                page.writeRight(
                        formatBetrag(
                                getBelegBetrag(beleg)
                        ),
                        tableX + tableWidth,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                nummer++;

                page.moveY(-ROW_HEIGHT);
            }

            page.ensureSpace(30f);

            page.moveY(-15f);

            page.writeRight(
                    "Anzahl Belege: "
                            + belege.size(),
                    tableX + tableWidth,
                    FONT_BOLD,
                    SECTION_SIZE
            );
        }
    }


    private void drawTableHeader(
            PDFPageWriter page,
            float x,
            float colBeleg,
            float colDatum,
            float colDokumente,
            float colBelegnummer,
            float colAussteller,
            float colBetrag,
            float colBeschreibung
    ) throws Exception {

        float xBeleg = x;
        float xDatum = xBeleg + colBeleg;
        float xDokumente = xDatum + colDatum;
        float xBelegnummer = xDokumente + colDokumente;
        float xAussteller = xBelegnummer + colBelegnummer;
        float xBeschreibung = xAussteller + colAussteller;
        float xBetrag = xBeschreibung + colBeschreibung;
        float tableRight = xBetrag + colBetrag;

        float y =
                page.getY();

        page.write(
                "Beleg",
                xBeleg + 3,
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
                "Belegnummer",
                xBelegnummer + 3,
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

        page.writeRight(
                "Betrag",
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
     * DOKUMENTGRÖSSE
     * =========================================================
     */

    private DocumentSize determineDocumentSize(
            byte[] content
    ) throws IOException {

        /*
         * =========================================================
         * PDF
         * =========================================================
         */

        if (isPdf(content)) {

            try (
                    PDDocument document =
                            org.apache.pdfbox.Loader.loadPDF(
                                    content
                            )
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

                return new DocumentSize(
                        box.getWidth(),
                        box.getHeight(),
                        PdfDocumentDensity.MEDIUM
                );
            }
        }

        /*
         * =========================================================
         * BILD
         * =========================================================
         */

        BufferedImage image =
                ImageIO.read(
                        new ByteArrayInputStream(
                                content
                        )
                );

        if (image != null) {

            ImageAnalysis analysis =
                    imageAnalysisService.analyze(
                            content
                    );

            float width =
                    image.getWidth();

            float height =
                    image.getHeight();

            /*
             * Bilder erhalten eine A4-nahe Referenzgröße.
             *
             * Die eigentliche Anpassung an die A4-Seite
             * übernimmt die A4LayoutEngine.
             */
            float scale =
                    IMAGE_REFERENCE_HEIGHT / height;

            /*
             * Kleine Bilder niemals künstlich vergrößern.
             */
            scale =
                    Math.min(
                            scale,
                            1f
                    );

            return new DocumentSize(
                    width * scale,
                    height * scale,
                    analysis.density()
            );
        }

        throw new IOException(
                "Dokument ist weder ein gültiges PDF "
                        + "noch ein unterstütztes Bildformat."
        );
    }


    private boolean isPdf(
            byte[] content
    ) {

        return content != null
                && content.length >= 5
                && content[0] == '%'
                && content[1] == 'P'
                && content[2] == 'D'
                && content[3] == 'F'
                && content[4] == '-';
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
    private BigDecimal getBelegBetrag(AbrechnungBeleg beleg) {
        return beleg.getPositionen()
                .stream()
                .map(AbrechnungBuchung::getBetrag)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}