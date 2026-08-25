package com.kcserver.service.pdf;

import com.kcserver.enumtype.PdfDocumentDensity;
import com.kcserver.enumtype.ReferenzObjekt;

public record PDFDocumentSize(
        float width,
        float height,
        PdfDocumentDensity density,
        ReferenzObjekt referenzObjekt
) {
}