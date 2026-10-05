package com.kcserver.service.pdf;

import com.kcserver.entity.beitraege.Zahlungsnachweis;

import java.util.List;

public record PDFBelegGruppe(
        int nummer,
        Zahlungsnachweis nachweis,
        List<A4LayoutItem> dokumente
) {
}