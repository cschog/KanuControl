package com.kcserver.enumtype;

import lombok.*;

@Getter
public enum PdfDokumentTyp {

    ANMELDUNG("Anmeldung"),
    DECKBLATT("Deckblatt"),
    ERHEBUNGSBOGEN("Erhebungsbogen"),
    TEILNEHMERLISTE("Teilnehmerliste"),
    ABRECHNUNG("Abrechnung");

    private final String label;

    PdfDokumentTyp(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
