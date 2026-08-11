package com.kcserver.service.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.Closeable;
import java.io.IOException;

/**
 * Verwaltet eine dynamisch erzeugte PDF-Seite.
 *
 * Aufgaben:
 * - neue DIN-A4-Seiten erzeugen
 * - ContentStream verwalten
 * - aktuelle Y-Position verwalten
 * - Text und Linien schreiben
 * - bei Seitenwechsel den bisherigen Stream sauber schließen
 *
 * Die eigentliche Fußzeile wird bewusst NICHT hier erzeugt.
 * Sie wird nach Fertigstellung des gesamten Dokuments über
 * PDFLayoutService ergänzt, damit "Seite X von Y" möglich ist.
 */
public class PDFPageWriter implements Closeable {

    private final PDDocument document;

    private PDPage page;

    private PDPageContentStream content;

    private float y;

    public PDFPageWriter(
            PDDocument document
    ) throws IOException {

        this.document = document;

        newPage();
    }

    /**
     * Erzeugt eine neue DIN-A4-Seite.
     *
     * Der bisherige ContentStream wird vorher sauber geschlossen.
     */
    public void newPage() throws IOException {

        closeContentStream();

        page =
                new PDPage(
                        PDFLayoutService.PAGE_SIZE
                );

        document.addPage(page);

        content =
                new PDPageContentStream(
                        document,
                        page
                );

        y =
                PDFLayoutService.PAGE_HEIGHT
                        - PDFLayoutService.MARGIN_TOP;
    }

    /**
     * Prüft, ob noch genügend Platz vorhanden ist.
     *
     * @param requiredHeight benötigte Höhe in Punkten
     * @return true, wenn der Inhalt noch auf die aktuelle Seite passt
     */
    public boolean hasSpace(
            float requiredHeight
    ) {

        return y - requiredHeight
                >= PDFLayoutService.MARGIN_BOTTOM;
    }

    /**
     * Sorgt dafür, dass genügend Platz vorhanden ist.
     * Falls nicht, wird automatisch eine neue Seite begonnen.
     *
     * @return true, wenn eine neue Seite erzeugt wurde
     */
    public boolean ensureSpace(
            float requiredHeight
    ) throws IOException {

        if (hasSpace(requiredHeight)) {
            return false;
        }

        newPage();
        return true;
    }

    /**
     * Aktuelle Y-Position.
     */
    public float getY() {
        return y;
    }

    /**
     * Setzt die aktuelle Y-Position.
     */
    public void setY(
            float y
    ) {
        this.y = y;
    }

    /**
     * Verändert die aktuelle Y-Position.
     */
    public void moveY(
            float delta
    ) {
        this.y += delta;
    }

    /**
     * Liefert die aktuelle Seite.
     */
    public PDPage getPage() {
        return page;
    }

    /**
     * Liefert den aktuellen ContentStream.
     */
    public PDPageContentStream getContent() {
        return content;
    }

    /**
     * Linker Rand.
     */
    public float getLeft() {
        return PDFLayoutService.MARGIN_LEFT;
    }

    /**
     * Rechter Rand.
     */
    public float getRight() {
        return PDFLayoutService.PAGE_WIDTH
                - PDFLayoutService.MARGIN_RIGHT;
    }

    /**
     * Breite des nutzbaren Inhaltsbereichs.
     */
    public float getContentWidth() {
        return PDFLayoutService.PAGE_WIDTH
                - PDFLayoutService.MARGIN_LEFT
                - PDFLayoutService.MARGIN_RIGHT;
    }

    /**
     * Schreibt Text.
     */
    public void write(
            String text,
            float x,
            float y,
            PDFont font,
            float fontSize
    ) throws IOException {

        if (text == null) {
            return;
        }

        content.beginText();

        content.setFont(
                font,
                fontSize
        );

        content.newLineAtOffset(
                x,
                y
        );

        content.showText(text);

        content.endText();
    }

    /**
     * Schreibt Text an der aktuellen Y-Position.
     */
    public void write(
            String text,
            float x,
            PDFont font,
            float fontSize
    ) throws IOException {

        write(
                text,
                x,
                y,
                font,
                fontSize
        );
    }

    /**
     * Schreibt Text rechtsbündig.
     */
    public void writeRight(
            String text,
            float rightX,
            float y,
            PDFont font,
            float fontSize
    ) throws IOException {

        if (text == null) {
            return;
        }

        float textWidth =
                font.getStringWidth(text)
                        / 1000f
                        * fontSize;

        write(
                text,
                rightX - textWidth,
                y,
                font,
                fontSize
        );
    }

    /**
     * Schreibt Text rechtsbündig an der aktuellen Y-Position.
     */
    public void writeRight(
            String text,
            float rightX,
            PDFont font,
            float fontSize
    ) throws IOException {

        writeRight(
                text,
                rightX,
                y,
                font,
                fontSize
        );
    }

    /**
     * Zeichnet eine horizontale Linie.
     */
    public void line(
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

    /**
     * Schließt den aktuellen ContentStream.
     */
    private void closeContentStream()
            throws IOException {

        if (content != null) {

            content.close();

            content = null;
        }
    }

    /**
     * Schließt den Writer.
     */
    @Override
    public void close()
            throws IOException {

        closeContentStream();
    }
}