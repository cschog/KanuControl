package com.kcserver.kjfp.service.pdf;

import com.kcserver.kjfp.entity.beitraege.Zahlungsnachweis;

import java.util.List;

public record PDFBelegGruppe(
        int nummer,
        Zahlungsnachweis nachweis,
        List<A4LayoutItem> dokumente
) {
}