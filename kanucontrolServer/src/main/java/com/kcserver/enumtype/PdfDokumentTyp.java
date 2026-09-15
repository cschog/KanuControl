package com.kcserver.enumtype;

import lombok.*;

@Getter
public enum PdfDokumentTyp {

    ANMELDUNG("Anmeldung"),
    DECKBLATT("Deckblatt"),
    ERHEBUNGSBOGEN("Erhebungsbogen"),
    TEILNEHMERLISTE("Teilnehmerliste"),
    TEILNEHMER_DATENKONTROLLE("Teilnehmer-Datenkontrolle"),
    ABRECHNUNG("Abrechnung"),
    REISEKOSTENABRECHNUNG("Fahrkostenabrechnung"),

    ZAHLUNGSNACHWEISE("Zahlungsnachweise"),
    FINANZAUSGLEICH("Finanzausgleich"),
    BELEGE("Belege");

    private final String label;

    PdfDokumentTyp(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
