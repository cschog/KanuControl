package com.kcserver.service.pdf;

public record DokumentZuordnung(
        String itemId,
        PDFBelegGruppe gruppe,
        A4LayoutItem dokument
) {
}