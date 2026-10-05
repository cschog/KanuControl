package com.kcserver.kjfp.service.pdf;

import com.kcserver.kjfp.enumtype.PdfDocumentDensity;

public record ImageAnalysis(
        int imageWidth,
        int imageHeight,
        float darkPixelDensity,
        float detailDensity,
        PdfDocumentDensity density
) {
}