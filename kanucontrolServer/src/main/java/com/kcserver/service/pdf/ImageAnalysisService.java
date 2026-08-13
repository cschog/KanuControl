package com.kcserver.service.pdf;

import com.kcserver.enumtype.PdfDocumentDensity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageAnalysisService {

    private static final int MAX_ANALYSIS_SIZE = 1000;

    private static final int DETAIL_THRESHOLD = 20;

    private static final int DARK_PIXEL_THRESHOLD = 160;


    public ImageAnalysis analyze(
            byte[] imageBytes
    ) throws IOException {

        BufferedImage image =
                ImageIO.read(
                        new ByteArrayInputStream(
                                imageBytes
                        )
                );

        if (image == null) {
            throw new IOException(
                    "Bild konnte nicht gelesen werden."
            );
        }

        int originalWidth =
                image.getWidth();

        int originalHeight =
                image.getHeight();

        BufferedImage analysisImage =
                resizeForAnalysis(image);


        float darkPixelDensity =
                calculateDarkPixelDensity(
                        analysisImage
                );

        float detailDensity =
                calculateDetailDensity(
                        analysisImage
                );

        PdfDocumentDensity density =
                determineDensity(
                        detailDensity
                );

        return new ImageAnalysis(
                originalWidth,
                originalHeight,
                darkPixelDensity,
                detailDensity,
                density
        );
    }


    /*
     * =========================================================
     * DENSITY
     * =========================================================
     */

    private PdfDocumentDensity determineDensity(
            float detailDensity
    ) {

        if (detailDensity < 0.20f) {

            return PdfDocumentDensity.LOW;

        } else if (detailDensity < 0.40f) {

            return PdfDocumentDensity.MEDIUM;

        } else {

            return PdfDocumentDensity.HIGH;
        }
    }


    /*
     * =========================================================
     * DARK PIXEL DENSITY
     * =========================================================
     */

    private float calculateDarkPixelDensity(
            BufferedImage image
    ) {

        long darkPixels = 0;
        long totalPixels = 0;

        int width =
                image.getWidth();

        int height =
                image.getHeight();

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                int gray =
                        image.getRaster()
                                .getSample(
                                        x,
                                        y,
                                        0
                                );

                if (gray < DARK_PIXEL_THRESHOLD) {
                    darkPixels++;
                }

                totalPixels++;
            }
        }

        if (totalPixels == 0) {
            return 0f;
        }

        return (float) darkPixels
                / totalPixels;
    }


    /*
     * =========================================================
     * DETAIL DENSITY
     * =========================================================
     */

    private float calculateDetailDensity(
            BufferedImage image
    ) {

        long detailPixels = 0;
        long totalComparisons = 0;

        int width =
                image.getWidth();

        int height =
                image.getHeight();


        /*
         * -----------------------------------------------------
         * Horizontal
         * -----------------------------------------------------
         */

        for (int y = 0; y < height; y++) {

            for (int x = 1; x < width; x++) {

                int current =
                        image.getRaster()
                                .getSample(
                                        x,
                                        y,
                                        0
                                );

                int previous =
                        image.getRaster()
                                .getSample(
                                        x - 1,
                                        y,
                                        0
                                );

                if (
                        Math.abs(
                                current - previous
                        )
                                >= DETAIL_THRESHOLD
                ) {

                    detailPixels++;
                }

                totalComparisons++;
            }
        }


        /*
         * -----------------------------------------------------
         * Vertikal
         * -----------------------------------------------------
         */

        for (int y = 1; y < height; y++) {

            for (int x = 0; x < width; x++) {

                int current =
                        image.getRaster()
                                .getSample(
                                        x,
                                        y,
                                        0
                                );

                int previous =
                        image.getRaster()
                                .getSample(
                                        x,
                                        y - 1,
                                        0
                                );

                if (
                        Math.abs(
                                current - previous
                        )
                                >= DETAIL_THRESHOLD
                ) {

                    detailPixels++;
                }

                totalComparisons++;
            }
        }

        if (totalComparisons == 0) {
            return 0f;
        }

        return (float) detailPixels
                / totalComparisons;
    }


    /*
     * =========================================================
     * SKALIERUNG
     * =========================================================
     */

    private BufferedImage resizeForAnalysis(
            BufferedImage image
    ) {

        int width =
                image.getWidth();

        int height =
                image.getHeight();

        int largest =
                Math.max(
                        width,
                        height
                );

        if (largest <= MAX_ANALYSIS_SIZE) {
            return image;
        }

        float scale =
                (float) MAX_ANALYSIS_SIZE
                        / largest;

        int newWidth =
                Math.max(
                        1,
                        Math.round(
                                width * scale
                        )
                );

        int newHeight =
                Math.max(
                        1,
                        Math.round(
                                height * scale
                        )
                );

        BufferedImage resized =
                new BufferedImage(
                        newWidth,
                        newHeight,
                        BufferedImage.TYPE_BYTE_GRAY
                );

        var graphics =
                resized.createGraphics();

        try {

            graphics.drawImage(
                    image,
                    0,
                    0,
                    newWidth,
                    newHeight,
                    null
            );

        } finally {

            graphics.dispose();
        }

        return resized;
    }
}