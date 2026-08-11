package com.kcserver.service.pdf;

import com.kcserver.entity.Veranstaltung;
import com.kcserver.entity.Zahlungsnachweis;
import com.kcserver.enumtype.PdfDokumentTyp;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.repository.abrechnung.ZahlungsnachweisRepository;
import com.kcserver.util.PdfFilenameUtil;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

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

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final NumberFormat MONEY =
            NumberFormat.getCurrencyInstance(
                    Locale.GERMANY
            );

    private final VeranstaltungRepository veranstaltungRepository;
    private final ZahlungsnachweisRepository zahlungsnachweisRepository;
    private final PDFLayoutService layoutService;

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
                PDDocument document =
                        new PDDocument();

                ByteArrayOutputStream out =
                        new ByteArrayOutputStream()
        ) {

            createDeckblatt(
                    document,
                    veranstaltung,
                    nachweise
            );

            /*
             * Footer erst nach Erzeugung
             * aller Seiten hinzufügen.
             */
            layoutService.addFooter(document);

            String filename =
                    PdfFilenameUtil.build(
                            LocalDate.now(),
                            PdfDokumentTyp.ZAHLUNGSNACHWEISE,
                            veranstaltung
                    );

            document.getDocumentInformation()
                    .setTitle(filename);

            document.getDocumentInformation()
                    .setAuthor("KanuControl");

            document.getDocumentInformation()
                    .setCreator("KanuControl");

            document.save(out);

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Zahlungsnachweise-PDF konnte nicht erzeugt werden.",
                    e
            );
        }
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

            float colBemerkung =
                    tableWidth
                            - colBeleg
                            - colDatum
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
                        truncate(
                                safe(
                                        nachweis.getBemerkung()
                                ),
                                65
                        ),
                        tableX
                                + colBeleg
                                + colDatum
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
                "Bemerkung",
                x
                        + colBeleg
                        + colDatum
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
}