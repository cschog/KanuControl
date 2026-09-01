package com.kcserver.service.pdf;

import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.PdfDokumentTyp;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.util.PdfFilenameUtil;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PDFTeilnehmerDatenkontrolleService {

    private static final PDType1Font FONT =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA
            );

    private static final PDType1Font FONT_BOLD =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_BOLD
            );

    /*
     * =========================================================
     * SEITE
     * =========================================================
     */

    private static final PDRectangle PAGE_SIZE =
            new PDRectangle(
                    PDRectangle.A4.getHeight(),
                    PDRectangle.A4.getWidth()
            );

    private static final float MARGIN_LEFT = 35f;
    private static final float MARGIN_RIGHT = 35f;
    private static final float MARGIN_TOP = 35f;
    private static final float MARGIN_BOTTOM = 30f;

    /*
     * =========================================================
     * SCHRIFTEN
     * =========================================================
     */

    private static final float TITLE_SIZE = 17f;
    private static final float SUBTITLE_SIZE = 9f;
    private static final float HEADER_SIZE = 8f;
    private static final float TEXT_SIZE = 8.5f;

    /*
     * =========================================================
     * TABELLE
     * =========================================================
     *
     * A4 quer:
     *
     * 841.9 x 595.3 pt
     *
     * Nutzbreite:
     *
     * 841.9 - 35 - 35 = 771.9 pt
     */

    private static final float ROW_HEIGHT = 43f;
    private static final float HEADER_HEIGHT = 25f;

    /*
     * Spalten:
     *
     * Verein
     * Name
     * Geburtsdatum
     * E-Mail
     * Straße
     * PLZ / Ort
     * Kontrolle
     */

    private static final float COL_VEREIN = 52f;
    private static final float COL_NAME = 145f;
    private static final float COL_GEBURT = 72f;
    private static final float COL_EMAIL = 185f;
    private static final float COL_STRASSE = 135f;
    private static final float COL_PLZ_ORT = 125f;
    private static final float COL_CHECK = 58f;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;

    /*
     * =========================================================
     * GENERATE
     * =========================================================
     */

    public byte[] generate(Long veranstaltungId) {

        Veranstaltung veranstaltung =
                veranstaltungRepository
                        .findById(veranstaltungId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Veranstaltung nicht gefunden."
                                )
                        );

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository
                        .findAllForDatenkontrolle(
                                veranstaltungId
                        );

        teilnehmer =
                sortTeilnehmer(teilnehmer);

        try (
                PDDocument document =
                        new PDDocument();

                ByteArrayOutputStream out =
                        new ByteArrayOutputStream()
        ) {

            int totalPages =
                    calculatePageCount(
                            teilnehmer
                    );

            int index = 0;

            for (int pageNumber = 1;
                 pageNumber <= totalPages;
                 pageNumber++) {

                PDPage page =
                        new PDPage(PAGE_SIZE);

                document.addPage(page);

                try (
                        PDPageContentStream content =
                                new PDPageContentStream(
                                        document,
                                        page
                                )
                ) {

                    float y =
                            PAGE_SIZE.getHeight()
                                    - MARGIN_TOP;

                    /*
                     * -------------------------------------------------
                     * KOPF
                     * -------------------------------------------------
                     */

                    y =
                            drawPageHeader(
                                    content,
                                    veranstaltung,
                                    pageNumber,
                                    totalPages,
                                    y
                            );

                    /*
                     * -------------------------------------------------
                     * TABELLE
                     * -------------------------------------------------
                     */

                    y =
                            drawTableHeader(
                                    content,
                                    y
                            );

                    /*
                     * -------------------------------------------------
                     * TEILNEHMER
                     * -------------------------------------------------
                     */

                    int rowsThisPage =
                            Math.min(
                                    getRowsPerPage(),
                                    teilnehmer.size() - index
                            );

                    for (int row = 0;
                         row < rowsThisPage;
                         row++) {

                        Teilnehmer tn =
                                teilnehmer.get(index++);

                        y =
                                drawTeilnehmerRow(
                                        content,
                                        tn,
                                        y
                                );
                    }
                }
            }

            String filename =
                    PdfFilenameUtil.build(
                            LocalDate.now(),
                            PdfDokumentTyp.TEILNEHMER_DATENKONTROLLE,
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

        } catch (IOException e) {

            throw new RuntimeException(
                    "Teilnehmer-Datenkontrolle-PDF konnte nicht erzeugt werden.",
                    e
            );
        }
    }

    /*
     * =========================================================
     * SEITENKOPF
     * =========================================================
     */

    private float drawPageHeader(
            PDPageContentStream content,
            Veranstaltung veranstaltung,
            int pageNumber,
            int totalPages,
            float y
    ) throws IOException {

        writeText(
                content,
                "Datenkontrolle Teilnehmer",
                MARGIN_LEFT,
                y,
                FONT_BOLD,
                TITLE_SIZE
        );

        writeRight(
                content,
                "Seite "
                        + pageNumber
                        + " von "
                        + totalPages,
                PAGE_SIZE.getWidth()
                        - MARGIN_RIGHT,
                y,
                FONT,
                SUBTITLE_SIZE
        );

        y -= 23f;

        String veranstaltungsText =
                safe(veranstaltung.getName());

        if (veranstaltung.getOrt() != null
                && !veranstaltung.getOrt().isBlank()) {

            veranstaltungsText +=
                    "  •  "
                            + veranstaltung.getOrt();
        }

        writeText(
                content,
                veranstaltungsText,
                MARGIN_LEFT,
                y,
                FONT_BOLD,
                SUBTITLE_SIZE
        );

        y -= 14f;

        String zeitraum =
                formatDate(
                        veranstaltung.getBeginnDatum()
                );

        if (veranstaltung.getEndeDatum() != null) {

            zeitraum +=
                    " - "
                            + formatDate(
                            veranstaltung.getEndeDatum()
                    );
        }

        writeText(
                content,
                "Zeitraum: " + zeitraum,
                MARGIN_LEFT,
                y,
                FONT,
                SUBTITLE_SIZE
        );

        y -= 15f;

        writeText(
                content,
                "Bitte alle persönlichen Daten prüfen. "
                        + "Korrekturen direkt in der Zeile eintragen "
                        + "und anschließend rechts abhaken.",
                MARGIN_LEFT,
                y,
                FONT,
                SUBTITLE_SIZE
        );

        return y - 12f;
    }

    /*
     * =========================================================
     * TABELLENKOPF
     * =========================================================
     */

    private float drawTableHeader(
            PDPageContentStream content,
            float y
    ) throws IOException {

        float x =
                MARGIN_LEFT;

        drawRect(
                content,
                x,
                y - HEADER_HEIGHT,
                getTableWidth(),
                HEADER_HEIGHT
        );

        writeCellHeader(
                content,
                "Verein",
                x,
                y,
                COL_VEREIN
        );

        x += COL_VEREIN;

        writeCellHeader(
                content,
                "Name, Vorname",
                x,
                y,
                COL_NAME
        );

        x += COL_NAME;

        writeCellHeader(
                content,
                "Geburtsdatum",
                x,
                y,
                COL_GEBURT
        );

        x += COL_GEBURT;

        writeCellHeader(
                content,
                "E-Mail",
                x,
                y,
                COL_EMAIL
        );

        x += COL_EMAIL;

        writeCellHeader(
                content,
                "Straße / Hausnr.",
                x,
                y,
                COL_STRASSE
        );

        x += COL_STRASSE;

        writeCellHeader(
                content,
                "PLZ / Ort",
                x,
                y,
                COL_PLZ_ORT
        );

        x += COL_PLZ_ORT;

        writeCellHeader(
                content,
                "geprüft",
                x,
                y,
                COL_CHECK
        );

        /*
         * Vertikale Linien
         */

        x = MARGIN_LEFT;

        float bottom =
                y - HEADER_HEIGHT;

        float[] columns = {
                COL_VEREIN,
                COL_NAME,
                COL_GEBURT,
                COL_EMAIL,
                COL_STRASSE,
                COL_PLZ_ORT
        };

        for (float width : columns) {

            x += width;

            line(
                    content,
                    x,
                    y,
                    x,
                    bottom
            );
        }

        return bottom;
    }

    /*
     * =========================================================
     * TEILNEHMERZEILE
     * =========================================================
     */

    private float drawTeilnehmerRow(
            PDPageContentStream content,
            Teilnehmer tn,
            float y
    ) throws IOException {

        Person person =
                tn.getPerson();

        float x =
                MARGIN_LEFT;

        float top = y;

        float bottom =
                y - ROW_HEIGHT;

        /*
         * -----------------------------------------------------
         * Außenrahmen
         * -----------------------------------------------------
         */

        drawRect(
                content,
                x,
                bottom,
                getTableWidth(),
                ROW_HEIGHT
        );

        /*
         * -----------------------------------------------------
         * VEREIN
         * -----------------------------------------------------
         */

        writeCell(
                content,
                getHauptvereinAbk(person),
                x,
                top,
                COL_VEREIN
        );

        x += COL_VEREIN;

        /*
         * -----------------------------------------------------
         * NAME
         * -----------------------------------------------------
         */

        writeCell(
                content,
                safe(person.getName())
                        + ", "
                        + safe(person.getVorname()),
                x,
                top,
                COL_NAME
        );

        x += COL_NAME;

        /*
         * -----------------------------------------------------
         * GEBURTSDATUM
         * -----------------------------------------------------
         */

        writeCell(
                content,
                formatDate(
                        person.getGeburtsdatum()
                ),
                x,
                top,
                COL_GEBURT
        );

        x += COL_GEBURT;

        /*
         * -----------------------------------------------------
         * E-MAIL
         * -----------------------------------------------------
         */

        writeCell(
                content,
                safe(person.getEmail()),
                x,
                top,
                COL_EMAIL
        );

        x += COL_EMAIL;

        /*
         * -----------------------------------------------------
         * STRASSE
         * -----------------------------------------------------
         */

        writeCell(
                content,
                safe(person.getStrasse()),
                x,
                top,
                COL_STRASSE
        );

        x += COL_STRASSE;

        /*
         * -----------------------------------------------------
         * PLZ / ORT
         * -----------------------------------------------------
         */

        writeCell(
                content,
                formatPlzOrt(person),
                x,
                top,
                COL_PLZ_ORT
        );

        x += COL_PLZ_ORT;

        /*
         * -----------------------------------------------------
         * CHECKBOX
         * -----------------------------------------------------
         */

        drawCheckbox(
                content,
                x,
                top
        );

        /*
         * -----------------------------------------------------
         * VERTIKALE TRENNLINIEN
         * -----------------------------------------------------
         */

        x = MARGIN_LEFT;

        float[] columns = {
                COL_VEREIN,
                COL_NAME,
                COL_GEBURT,
                COL_EMAIL,
                COL_STRASSE,
                COL_PLZ_ORT
        };

        for (float width : columns) {

            x += width;

            line(
                    content,
                    x,
                    top,
                    x,
                    bottom
            );
        }

        return bottom;
    }

    /*
     * =========================================================
     * CHECKBOX
     * =========================================================
     */

    private void drawCheckbox(
            PDPageContentStream content,
            float x,
            float top
    ) throws IOException {

        float size = 13f;

        float boxX =
                x
                        + (COL_CHECK - size) / 2f;

        float boxY =
                top
                        - (ROW_HEIGHT + size) / 2f;

        drawRect(
                content,
                boxX,
                boxY,
                size,
                size
        );
    }

    /*
     * =========================================================
     * HAUPTVEREIN
     * =========================================================
     */

    private String getHauptvereinAbk(
            Person person
    ) {

        if (person == null
                || person.getMitgliedschaften() == null) {

            return "";
        }

        return person.getMitgliedschaften()
                .stream()
                .filter(
                        Mitglied::getHauptVerein
                )
                .filter(
                        m -> m.getVerein() != null
                )
                .map(
                        m -> m.getVerein().getAbk()
                )
                .filter(
                        abk -> abk != null
                                && !abk.isBlank()
                )
                .findFirst()
                .orElse("");
    }

    /*
     * =========================================================
     * SORTIERUNG
     * =========================================================
     */

    private List<Teilnehmer> sortTeilnehmer(
            List<Teilnehmer> list
    ) {

        return list.stream()
                .filter(
                        t -> t.getPerson() != null
                )
                .sorted(
                        Comparator
                                .comparing(
                                        (Teilnehmer t) ->
                                                safe(
                                                        t.getPerson()
                                                                .getName()
                                                ),
                                        String.CASE_INSENSITIVE_ORDER
                                )
                                .thenComparing(
                                        t ->
                                                safe(
                                                        t.getPerson()
                                                                .getVorname()
                                                ),
                                        String.CASE_INSENSITIVE_ORDER
                                )
                )
                .toList();
    }

    /*
     * =========================================================
     * SEITENBERECHNUNG
     * =========================================================
     */

    private int getRowsPerPage() {

        float pageHeight =
                PAGE_SIZE.getHeight();

        float available =
                pageHeight
                        - MARGIN_TOP
                        - MARGIN_BOTTOM
                        - 100f;

        return Math.max(
                1,
                (int) (
                        available
                                / ROW_HEIGHT
                )
        );
    }

    private int calculatePageCount(
            List<Teilnehmer> teilnehmer
    ) {

        if (teilnehmer.isEmpty()) {
            return 1;
        }

        return (int) Math.ceil(
                (double) teilnehmer.size()
                        / getRowsPerPage()
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

    private String formatPlzOrt(
            Person person
    ) {

        String plz =
                safe(person.getPlz());

        String ort =
                safe(person.getOrt());

        if (plz.isBlank()) {
            return ort;
        }

        if (ort.isBlank()) {
            return plz;
        }

        return plz + " " + ort;
    }

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }

    /*
     * =========================================================
     * PDF HELPER
     * =========================================================
     */

    private float getTableWidth() {

        return COL_VEREIN
                + COL_NAME
                + COL_GEBURT
                + COL_EMAIL
                + COL_STRASSE
                + COL_PLZ_ORT
                + COL_CHECK;
    }

    private void writeCellHeader(
            PDPageContentStream content,
            String text,
            float x,
            float top,
            float width
    ) throws IOException {

        writeText(
                content,
                text,
                x + 4f,
                top - 16f,
                FONT_BOLD,
                HEADER_SIZE
        );
    }

    private void writeCell(
            PDPageContentStream content,
            String text,
            float x,
            float top,
            float width
    ) throws IOException {

        writeText(
                content,
                text,
                x + 4f,
                top - 17f,
                FONT,
                TEXT_SIZE
        );
    }

    private void writeText(
            PDPageContentStream content,
            String text,
            float x,
            float y,
            PDType1Font font,
            float size
    ) throws IOException {

        content.beginText();
        content.setFont(font, size);
        content.newLineAtOffset(x, y);
        content.showText(
                sanitize(text)
        );
        content.endText();
    }

    private void writeRight(
            PDPageContentStream content,
            String text,
            float x,
            float y,
            PDType1Font font,
            float size
    ) throws IOException {

        String value =
                sanitize(text);

        float width =
                font.getStringWidth(value)
                        / 1000f
                        * size;

        writeText(
                content,
                value,
                x - width,
                y,
                font,
                size
        );
    }

    private void line(
            PDPageContentStream content,
            float x1,
            float y1,
            float x2,
            float y2
    ) throws IOException {

        content.moveTo(
                x1,
                y1
        );

        content.lineTo(
                x2,
                y2
        );

        content.stroke();
    }

    private void drawRect(
            PDPageContentStream content,
            float x,
            float y,
            float width,
            float height
    ) throws IOException {

        content.addRect(
                x,
                y,
                width,
                height
        );

        content.stroke();
    }

    private String sanitize(
            String value
    ) {

        if (value == null) {
            return "";
        }

        /*
         * Helvetica / WinAnsi kann viele Unicode-Zeichen
         * nicht darstellen.
         *
         * Für diesen Kontrollreport ersetzen wir zunächst
         * problematische Sonderzeichen.
         */

        return value
                .replace("–", "-")
                .replace("—", "-")
                .replace("’", "'")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("•", "-");
    }
}