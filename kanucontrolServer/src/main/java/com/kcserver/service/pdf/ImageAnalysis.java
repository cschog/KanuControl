package com.kcserver.service.pdf;

import com.kcserver.enumtype.PdfDocumentDensity;

public record ImageAnalysis(
        int imageWidth,
        int imageHeight,
        float darkPixelDensity,
        float detailDensity,
        PdfDocumentDensity density
) {
}