package com.kcserver.kjfp.service.pdf;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.awt.Color;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

class BelegnummerHintergrundPdfTest {
    @Test void backgroundCoversCompleteNumberAndPaddingOnDarkDocuments() throws Exception {
        try (var pdf = new PDDocument()) {
            var composer = new PDFDocumentComposer(new PDFLayoutService());
            for (var number : new String[]{"RZ_009", "Hil_028", "LANGES_KONTO_12345"}) {
                var page = new PDPage(); pdf.addPage(page);
                try (var stream = new PDPageContentStream(pdf, page)) {
                    stream.setNonStrokingColor(Color.DARK_GRAY);
                    stream.addRect(0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight()); stream.fill();
                }
                var placement = new A4LayoutPlacement("doc", pdf.getNumberOfPages(), 40, 100, 400, 500, 0, 500, 0, false);
                ReflectionTestUtils.invokeMethod(composer, "drawBelegNummer", pdf, placement, Map.of("doc", number));
                var image = new PDFRenderer(pdf).renderImage(pdf.getNumberOfPages()-1, 2);
                var font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                float width = font.getStringWidth("Beleg " + number) / 1000f * 8;
                int sampleY = Math.round((page.getMediaBox().getHeight() - (584 - 2)) * 2);
                assertThat(new Color(image.getRGB(Math.round((46 + width + 1) * 2), sampleY)))
                        .as("Hintergrund bis hinter das letzte Zeichen: " + number).isEqualTo(Color.WHITE);
                assertThat(new Color(image.getRGB(42 * 2, sampleY))).isEqualTo(Color.DARK_GRAY);
            }
            if (System.getProperty("composer.preview") != null) pdf.save(System.getProperty("composer.preview"));
        }
    }
}
