package com.kcserver.kjfp.service.pdf;

import com.kcserver.kjfp.enumtype.PdfDocumentDensity;
import com.kcserver.kjfp.enumtype.ReferenzObjekt;

public record PDFDocumentSize(
        float width,
        float height,
        PdfDocumentDensity density,
        ReferenzObjekt referenzObjekt
) {
}