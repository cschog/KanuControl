package com.kcserver.enumtype;

import lombok.Getter;

@Getter
public enum FinanzKategorie {

    /* ================= PLAN + IST KOSTEN ================= */

    UNTERKUNFT(
            "Unterkunft",
            FinanzTyp.KOSTEN,
            true,
            10
    ),

    VERPFLEGUNG(
            "Verpflegung",
            FinanzTyp.KOSTEN,
            true,
            20
    ),

    HONORARE(
            "Honorare",
            FinanzTyp.KOSTEN,
            false,
            30
    ),

    FAHRTKOSTEN(
            "Fahrtkosten",
            FinanzTyp.KOSTEN,
            false,
            40
    ),

    VERBRAUCHSMATERIAL(
            "Verbrauchsmaterial",
            FinanzTyp.KOSTEN,
            false,
            50
    ),

    KULTUR(
            "Kultur / Programm",
            FinanzTyp.KOSTEN,
            false,
            60
    ),

    MIETE(
            "Miete",
            FinanzTyp.KOSTEN,
            false,
            70
    ),

    SONSTIGE_KOSTEN(
            "Sonstige Kosten",
            FinanzTyp.KOSTEN,
            false,
            80
    ),

    /* ================= EINNAHMEN ================= */

    TEILNEHMERBEITRAG(
            "Teilnehmerbeiträge",
            FinanzTyp.EINNAHME,
            false,
            110
    ),

    PFAND(
            "Pfand",
            FinanzTyp.EINNAHME,
            false,
            120
    ),

    KJFP_ZUSCHUSS(
            "KJFP-Zuschuss",
            FinanzTyp.EINNAHME,
            false,
            130
    ),

    EIGENANTEIL(
            "Eigenanteil Verein",
            FinanzTyp.EINNAHME,
            true,
            150
    ),

    SONSTIGE_EINNAHMEN(
            "Sonstige Einnahmen",
            FinanzTyp.EINNAHME,
            false,
            140
    );

    private final String bezeichnung;

    private final FinanzTyp typ;

    /**
     * Wird diese Position automatisch berechnet?
     */
    private final boolean automatischBerechnet;

    /**
     * Reihenfolge in der Anzeige.
     */
    private final int sortierung;

    FinanzKategorie(
            String bezeichnung,
            FinanzTyp typ,
            boolean automatischBerechnet,
            int sortierung) {

        this.bezeichnung = bezeichnung;
        this.typ = typ;
        this.automatischBerechnet = automatischBerechnet;
        this.sortierung = sortierung;
    }

    public boolean isKosten() {
        return typ == FinanzTyp.KOSTEN;
    }

    public boolean isEinnahme() {
        return typ == FinanzTyp.EINNAHME;
    }
}