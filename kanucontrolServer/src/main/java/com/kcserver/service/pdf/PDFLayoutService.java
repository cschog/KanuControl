package com.kcserver.service.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;

@Service
public class PDFLayoutService {

    /*
     * =========================================================
     * SEITENLAYOUT
     * =========================================================
     */

    public static final float MARGIN_LEFT = 40f;
    public static final float MARGIN_RIGHT = 40f;
    public static final float MARGIN_TOP = 45f;
    public static final float MARGIN_BOTTOM = 42f;

    /*
     * =========================================================
     * SEITENFORMATE
     * =========================================================
     */

    public static final PDRectangle PAGE_SIZE =
            PDRectangle.A4;

    public static final float PAGE_WIDTH =
            PAGE_SIZE.getWidth();

    public static final float PAGE_HEIGHT =
            PAGE_SIZE.getHeight();

    public static final float A4_WIDTH =
            PAGE_SIZE.getWidth();

    public static final float A4_HEIGHT =
            PAGE_SIZE.getHeight();

    public static final float A5_WIDTH =
            PDRectangle.A5.getWidth();

    public static final float A5_HEIGHT =
            PDRectangle.A5.getHeight();

    public static final float A6_WIDTH =
            PDRectangle.A6.getWidth();

    public static final float A6_HEIGHT =
            PDRectangle.A6.getHeight();

    /*
     * =========================================================
     * FOOTER
     * =========================================================
     */

    /*
     * 8 mm = 22,68 pt
     */
    private static final float LOGO_WIDTH = 22.68f;

    private static final float FOOTER_Y = 18f;

    private static final float FOOTER_FONT_SIZE = 8f;

    private static final PDType1Font FOOTER_FONT =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA
            );

    /*
     * =========================================================
     * FOOTER
     * =========================================================
     */

    /**
     * Fügt allen vorhandenen Seiten den gemeinsamen Footer hinzu.
     *
     * Muss erst nach Erzeugung aller Seiten aufgerufen werden,
     * damit "Seite X von Y" möglich ist.
     */
    public void addFooter(
            PDDocument document
    ) throws IOException {

        int pageCount =
                document.getNumberOfPages();

        if (pageCount == 0) {
            return;
        }

        byte[] logoBytes =
                StreamUtils.copyToByteArray(
                        new ClassPathResource(
                                "pdf/logoKanuControl.png"
                        ).getInputStream()
                );

        PDImageXObject logo =
                PDImageXObject.createFromByteArray(
                        document,
                        logoBytes,
                        "KanuControl Logo"
                );

        for (int i = 0; i < pageCount; i++) {

            PDPage page =
                    document.getPage(i);

            addFooter(
                    document,
                    page,
                    logo,
                    i + 1,
                    pageCount
            );
        }
    }

    private void addFooter(
            PDDocument document,
            PDPage page,
            PDImageXObject logo,
            int pageNumber,
            int pageCount
    ) throws IOException {

        PDRectangle mediaBox =
                page.getMediaBox();

        float pageWidth =
                mediaBox.getWidth();

        /*
         * Logo proportional auf 8 mm Breite skalieren.
         */
        float logoHeight =
                logo.getHeight()
                        * LOGO_WIDTH
                        / logo.getWidth();

        float logoX =
                pageWidth
                        - MARGIN_RIGHT
                        - LOGO_WIDTH;

        float logoY =
                FOOTER_Y;

        String pageText =
                "Seite "
                        + pageNumber
                        + " von "
                        + pageCount;

        float textWidth =
                FOOTER_FONT.getStringWidth(pageText)
                        / 1000f
                        * FOOTER_FONT_SIZE;

        float textX =
                logoX
                        - 6f
                        - textWidth;

        float textY =
                FOOTER_Y + 4f;

        try (
                PDPageContentStream content =
                        new PDPageContentStream(
                                document,
                                page,
                                PDPageContentStream.AppendMode.APPEND,
                                true,
                                true
                        )
        ) {

            /*
             * Seitennummer
             */
            content.beginText();

            content.setFont(
                    FOOTER_FONT,
                    FOOTER_FONT_SIZE
            );

            content.newLineAtOffset(
                    textX,
                    textY
            );

            content.showText(pageText);

            content.endText();

            /*
             * Logo
             */
            content.drawImage(
                    logo,
                    logoX,
                    logoY,
                    LOGO_WIDTH,
                    logoHeight
            );
        }
    }

    /*
     * =========================================================
     * NUTZFLÄCHE
     * =========================================================
     */

    public float getContentWidth() {

        return A4_WIDTH
                - MARGIN_LEFT
                - MARGIN_RIGHT;
    }

    public float getContentHeight() {

        return A4_HEIGHT
                - MARGIN_TOP
                - MARGIN_BOTTOM;
    }

    public float getContentTop() {

        return A4_HEIGHT
                - MARGIN_TOP;
    }

    public float getContentBottom() {

        return MARGIN_BOTTOM;
    }

    public float getMarginLeft() {
        return MARGIN_LEFT;
    }

    public float getMarginBottom() {
        return MARGIN_BOTTOM;
    }

    /*
     * =========================================================
     * SEITENGRÖSSE
     * =========================================================
     */

    public static float getPageWidth() {

        return A4_WIDTH;
    }

    public static float getPageHeight() {

        return A4_HEIGHT;
    }
}