package com.kcserver.service.pdf;

import com.kcserver.dto.finanzen.FinanzausgleichDTO;
import com.kcserver.entity.Dokument;
import com.kcserver.entity.FinanzGruppe;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.PdfDokumentTyp;
import com.kcserver.exception.ErrorMessages;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.repository.abrechnung.DokumentRepository;
import com.kcserver.repository.finanz.FinanzGruppeRepository;
import com.kcserver.service.finanz.FinanzausgleichService;
import com.kcserver.util.PdfFilenameUtil;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PDFFinanzausgleichService {

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

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final NumberFormat MONEY =
            NumberFormat.getCurrencyInstance(
                    Locale.GERMANY
            );

    private final VeranstaltungRepository veranstaltungRepository;
    private final FinanzGruppeRepository finanzGruppeRepository;
    private final DokumentRepository dokumentRepository;

    private final FinanzausgleichService finanzausgleichService;

    private final PDFDocumentSizeService documentSizeService;
    private final PDFLayoutService layoutService;
    private final A4LayoutEngine layoutEngine;
    private final PDFDocumentComposer composer;


    /*
     * =========================================================
     * INTERNE DATENSTRUKTUREN
     * =========================================================
     */

    record FinanzausgleichGruppe(
            int nummer,
            FinanzGruppe finanzGruppe,
            FinanzausgleichDTO ausgleich,
            List<Dokument> dokumente,
            List<A4LayoutItem> layoutItems
    ) {
    }

    record PlatzierteDokumentZuordnung(
            A4LayoutPlacement placement,
            FinanzausgleichGruppe gruppe,
            Dokument dokument
    ) {
    }


    /*
     * =========================================================
     * GENERATE
     * =========================================================
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
                                        ErrorMessages.VERANSTALTUNG_NOT_FOUND
                                )
                        );

        /*
         * -----------------------------------------------------
         * 1. FINANZAUSGLEICH BERECHNEN
         * -----------------------------------------------------
         */

        List<FinanzausgleichDTO> ausgleiche =
                finanzausgleichService.berechne(
                        veranstaltungId
                );

        /*
         * VK ist keine Finanzausgleichsgruppe.
         */
        ausgleiche =
                ausgleiche.stream()
                        .filter(dto ->
                                !"VK".equals(
                                        dto.getFinanzGruppeKuerzel()
                                )
                        )
                        .toList();

        try (
                PDDocument deckblatt =
                        new PDDocument()
        ) {

            /*
             * -------------------------------------------------
             * 2. GRUPPEN ERZEUGEN
             * -------------------------------------------------
             */

            List<FinanzausgleichGruppe> gruppen =
                    createGruppen(
                            ausgleiche
                    );

            /*
             * -------------------------------------------------
             * 3. DECKBLATT
             * -------------------------------------------------
             */

            createDeckblatt(
                    deckblatt,
                    veranstaltung,
                    gruppen
            );

            /*
             * -------------------------------------------------
             * 4. DOKUMENTE SAMMELN
             * -------------------------------------------------
             */

            Map<String, byte[]> documents =
                    collectDocuments(
                            gruppen
                    );

            /*
             * -------------------------------------------------
             * 5. LAYOUT ITEMS
             * -------------------------------------------------
             */

            List<A4LayoutItem> items =
                    createLayoutItems(
                            gruppen
                    );

            /*
             * -------------------------------------------------
             * 6. LAYOUT
             * -------------------------------------------------
             */

            List<A4LayoutPlacement> placements =
                    layoutEngine.layout(
                            items
                    );

            /*
             * -------------------------------------------------
             * 7. DOKUMENT-ZUORDNUNGEN
             * -------------------------------------------------
             */

            Map<String, String> gruppenNummern =
                    createGruppenNummern(
                            gruppen
                    );

            /*
             * -------------------------------------------------
             * 8. DOKUMENTE ZUSAMMENSETZEN
             * -------------------------------------------------
             */

            byte[] dokumentPdf =
                    composer.composeWithoutFooter(
                            documents,
                            placements,
                            gruppenNummern
                    );

            /*
             * -------------------------------------------------
             * 9. DECKBLATT + DOKUMENTE
             * -------------------------------------------------
             */

            byte[] gesamtesPdf =
                    mergeDocuments(
                            deckblatt,
                            dokumentPdf
                    );

            /*
             * -------------------------------------------------
             * 10. FOOTER
             * -------------------------------------------------
             */

            String filename =
                    PdfFilenameUtil.build(
                            LocalDate.now(),
                            PdfDokumentTyp.FINANZAUSGLEICH,
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
                    "Finanzausgleich-PDF konnte nicht erzeugt werden.",
                    e
            );
        }
    }


    /*
     * =========================================================
     * GRUPPEN
     * =========================================================
     */

    private List<FinanzausgleichGruppe> createGruppen(
            List<FinanzausgleichDTO> ausgleiche
    ) throws IOException {

        List<FinanzausgleichGruppe> result =
                new ArrayList<>();

        int nummer = 1;

        for (FinanzausgleichDTO ausgleich : ausgleiche) {

            FinanzGruppe finanzGruppe =
                    finanzGruppeRepository
                            .findById(
                                    ausgleich.getFinanzGruppeId()
                            )
                            .orElseThrow(() ->
                                    new IOException(
                                            "FinanzGruppe nicht gefunden: "
                                                    + ausgleich.getFinanzGruppeId()
                                    )
                            );

            List<Dokument> dokumente =
                    dokumentRepository
                            .findByFinanzGruppeIdOrderByReihenfolgeAsc(
                                    ausgleich.getFinanzGruppeId()
                            );

            List<A4LayoutItem> layoutItems =
                    new ArrayList<>();

            for (Dokument dokument : dokumente) {

                String itemId =
                        "FA-"
                                + finanzGruppe.getId()
                                + "-DOC-"
                                + dokument.getId();

                PDFDocumentSize size;

                try {

                    size =
                            documentSizeService.determine(
                                    dokument
                            );

                } catch (IllegalStateException e) {

                    throw new IOException(
                            "Keine Dokumentgröße hinterlegt: "
                                    + "FinanzGruppe "
                                    + finanzGruppe.getId()
                                    + ", Dokument "
                                    + dokument.getId()
                                    + ", Dateiname "
                                    + dokument.getOriginalDateiname(),
                            e
                    );
                }

                layoutItems.add(
                        new A4LayoutItem(
                                itemId,
                                size.width(),
                                size.height(),
                                size.density(),
                                size.referenzObjekt()
                        )
                );
            }

            result.add(
                    new FinanzausgleichGruppe(
                            nummer,
                            finanzGruppe,
                            ausgleich,
                            dokumente,
                            layoutItems
                    )
            );

            nummer++;
        }

        return result;
    }


    /*
     * =========================================================
     * DECKBLATT
     * =========================================================
     */

    private void createDeckblatt(
            PDDocument document,
            Veranstaltung veranstaltung,
            List<FinanzausgleichGruppe> gruppen
    ) throws Exception {

        try (
                PDFPageWriter page =
                        new PDFPageWriter(document)
        ) {

            page.write(
                    "Finanzausgleich",
                    page.getLeft(),
                    FONT_BOLD,
                    TITLE_SIZE
            );

            page.moveY(-35f);

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
             * HINWEIS
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
                    "Die Nachweise sind zusammen mit der Abrechnung bei KVNRW einzureichen.",
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

            float colNummer = 32f;
            float colGruppe = 45f;
            float colSoll = 65f;
            float colUeberweisung = 70f;
            float colQuittung = 60f;
            float colAusgaben = 65f;
            float colFahrkosten = 65f;

            float colAusgleich =
                    tableWidth
                            - colNummer
                            - colGruppe
                            - colSoll
                            - colUeberweisung
                            - colQuittung
                            - colAusgaben
                            - colFahrkosten;

            drawTableHeader(
                    page,
                    tableX,
                    colNummer,
                    colGruppe,
                    colSoll,
                    colUeberweisung,
                    colQuittung,
                    colAusgaben,
                    colFahrkosten,
                    colAusgleich
            );

            page.moveY(-ROW_HEIGHT);

            BigDecimal gesamt =
                    BigDecimal.ZERO;

            for (FinanzausgleichGruppe gruppe : gruppen) {

                boolean neueSeite =
                        page.ensureSpace(
                                ROW_HEIGHT
                        );

                if (neueSeite) {

                    drawTableHeader(
                            page,
                            tableX,
                            colNummer,
                            colGruppe,
                            colSoll,
                            colUeberweisung,
                            colQuittung,
                            colAusgaben,
                            colFahrkosten,
                            colAusgleich
                    );

                    page.moveY(-ROW_HEIGHT);
                }

                float y =
                        page.getY();

                FinanzausgleichDTO dto =
                        gruppe.ausgleich();

                page.write(
                        String.format("#%02d", gruppe.nummer()),
                        tableX + 3,
                        y - 14,
                        FONT_BOLD,
                        TEXT_SIZE
                );

                float x = tableX + colNummer;

                page.write(
                        dto.getFinanzGruppeKuerzel(),
                        x + 3,
                        y - 14,
                        FONT_BOLD,
                        TEXT_SIZE
                );

                x += colGruppe;

                page.writeRight(
                        formatMoney(
                                dto.getTeilnehmerBeitraegeSoll()
                        ),
                        x + colSoll - 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                x += colSoll;

                page.writeRight(
                        formatMoney(
                                dto.getTeilnehmerBeitraegeUeberweisung()
                        ),
                        x + colUeberweisung - 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                x += colUeberweisung;

                page.writeRight(
                        formatMoney(
                                dto.getTeilnehmerBeitraegeQuittung()
                        ),
                        x + colQuittung - 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                x += colQuittung;

                page.writeRight(
                        formatMoney(
                                dto.getAusgaben()
                        ),
                        x + colAusgaben - 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                x += colAusgaben;

                page.writeRight(
                        formatMoney(
                                dto.getFahrkosten()
                        ),
                        x + colFahrkosten - 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                x += colFahrkosten;

                page.writeRight(
                        formatMoney(
                                dto.getErstattungVomVK()
                        ),
                        x + colAusgleich - 3,
                        y - 14,
                        FONT_BOLD,
                        TEXT_SIZE
                );

                page.line(
                        tableX,
                        y - ROW_HEIGHT,
                        tableX + tableWidth,
                        y - ROW_HEIGHT
                );

                gesamt =
                        gesamt.add(
                                safe(
                                        dto.getErstattungVomVK()
                                )
                        );

                page.moveY(-ROW_HEIGHT);
            }

            /*
             * -------------------------------------------------
             * GESAMT
             * -------------------------------------------------
             */

            page.ensureSpace(30f);

            page.moveY(-15f);

            page.writeRight(
                    "Gesamterstattung: "
                            + formatMoney(gesamt),
                    tableX + tableWidth,
                    FONT_BOLD,
                    SECTION_SIZE
            );

            /*
             * -------------------------------------------------
             * FEHLENDE NACHWEISE
             * -------------------------------------------------
             */

            long fehlendeDokumente =
                    gruppen.stream()
                            .filter(gruppe ->
                                    gruppe.dokumente()
                                            .isEmpty()
                            )
                            .count();

            if (fehlendeDokumente > 0) {

                page.ensureSpace(30f);

                page.moveY(-15f);

                String hinweis =
                        fehlendeDokumente == 1
                                ? "Hinweis: Bei einer Finanzgruppe "
                                + "fehlt noch der Nachweis."
                                : "Hinweis: Bei "
                                + fehlendeDokumente
                                + " Finanzgruppen fehlen noch "
                                + "Nachweisdokumente.";

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


    /*
     * =========================================================
     * TABELLENKOPF
     * =========================================================
     */

    private void drawTableHeader(
            PDFPageWriter page,
            float x,
            float colNummer,
            float colGruppe,
            float colSoll,
            float colUeberweisung,
            float colQuittung,
            float colAusgaben,
            float colFahrkosten,
            float colAusgleich
    ) throws Exception {

        float y =
                page.getY();

        /*
         * -----------------------------------------------------
         * Nummer
         * -----------------------------------------------------
         */

        page.write(
                "Nr.",
                x + 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        /*
         * -----------------------------------------------------
         * Gruppe
         * -----------------------------------------------------
         */

        float currentX =
                x + colNummer;

        page.write(
                "Gruppe",
                currentX + 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colGruppe;

        /*
         * -----------------------------------------------------
         * Soll
         * -----------------------------------------------------
         */

        page.writeRight(
                "Soll",
                currentX + colSoll - 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colSoll;

        /*
         * -----------------------------------------------------
         * Überweisung
         * -----------------------------------------------------
         */

        page.writeRight(
                "Überweisung",
                currentX + colUeberweisung - 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colUeberweisung;

        /*
         * -----------------------------------------------------
         * Quittung
         * -----------------------------------------------------
         */

        page.writeRight(
                "Quittung",
                currentX + colQuittung - 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colQuittung;

        /*
         * -----------------------------------------------------
         * Ausgaben
         * -----------------------------------------------------
         */

        page.writeRight(
                "Ausgaben",
                currentX + colAusgaben - 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colAusgaben;

        /*
         * -----------------------------------------------------
         * Fahrtkosten
         * -----------------------------------------------------
         */

        page.writeRight(
                "Fahrtkosten",
                currentX + colFahrkosten - 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colFahrkosten;

        /*
         * -----------------------------------------------------
         * Ausgleich
         * -----------------------------------------------------
         */

        page.writeRight(
                "Ausgleich",
                currentX + colAusgleich - 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.line(
                x,
                y - ROW_HEIGHT,
                currentX + colAusgleich,
                y - ROW_HEIGHT
        );
    }


    /*
     * =========================================================
     * DOKUMENTE
     * =========================================================
     */

    private Map<String, byte[]> collectDocuments(
            List<FinanzausgleichGruppe> gruppen
    ) {

        Map<String, byte[]> documents =
                new LinkedHashMap<>();

        for (FinanzausgleichGruppe gruppe : gruppen) {

            for (Dokument dokument :
                    gruppe.dokumente()) {

                String id =
                        "FA-"
                                + gruppe.finanzGruppe().getId()
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
            List<FinanzausgleichGruppe> gruppen
    ) {

        List<A4LayoutItem> items =
                new ArrayList<>();

        for (FinanzausgleichGruppe gruppe : gruppen) {

            items.addAll(
                    gruppe.layoutItems()
            );
        }

        return items;
    }


    private Map<String, String> createGruppenNummern(
            List<FinanzausgleichGruppe> gruppen
    ) {

        Map<String, String> result =
                new LinkedHashMap<>();

        for (FinanzausgleichGruppe gruppe : gruppen) {

            String nummer =
                    String.format(
                            "#%02d",
                            gruppe.nummer()
                    );

            for (A4LayoutItem dokument :
                    gruppe.layoutItems()) {

                result.put(
                        dokument.id(),
                        nummer
                );
            }
        }

        return result;
    }


    /*
     * =========================================================
     * DECKBLATT + DOKUMENTE
     * =========================================================
     */

    private byte[] mergeDocuments(
            PDDocument deckblatt,
            byte[] dokumentPdf
    ) throws IOException {

        if (dokumentPdf == null
                || dokumentPdf.length == 0) {

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            deckblatt.save(out);

            return out.toByteArray();
        }

        try (
                PDDocument dokumente =
                        org.apache.pdfbox.Loader.loadPDF(
                                dokumentPdf
                        );

                PDDocument gesamt =
                        new PDDocument();

                ByteArrayOutputStream out =
                        new ByteArrayOutputStream()
        ) {

            LayerUtility layerUtility =
                    new LayerUtility(gesamt);

            /*
             * DECKBLATT
             */

            int deckblattIndex = 0;

            for (PDPage ignored :
                    deckblatt.getPages()) {

                var form =
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
             * DOKUMENTE
             */

            for (int i = 0;
                 i < dokumente.getNumberOfPages();
                 i++) {

                var form =
                        layerUtility.importPageAsForm(
                                dokumente,
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
                safe(value)
        );
    }


    private BigDecimal safe(
            BigDecimal value
    ) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }


    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }
}