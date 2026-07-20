package com.kcserver.enumtype;

import lombok.Getter;

@Getter
public enum FinanzKategorie {

    /* ================= PLAN + IST KOSTEN ================= */

    UNTERKUNFT(
            "Unterkunft",
            FinanzTyp.KOSTEN,
            10
    ),

    VERPFLEGUNG(
            "Verpflegung",
            FinanzTyp.KOSTEN,
            20
    ),

    HONORARE(
            "Honorare",
            FinanzTyp.KOSTEN,
            30
    ),

    FAHRTKOSTEN(
            "Fahrtkosten",
            FinanzTyp.KOSTEN,
            40
    ),

    VERBRAUCHSMATERIAL(
            "Verbrauchsmaterial",
            FinanzTyp.KOSTEN,
            50
    ),

    KULTUR(
            "Kultur / Programm",
            FinanzTyp.KOSTEN,
            60
    ),

    MIETE(
            "Miete",
            FinanzTyp.KOSTEN,
            70
    ),

    SONSTIGE_KOSTEN(
            "Sonstige Kosten",
            FinanzTyp.KOSTEN,
            80
    ),

    /* ================= EINNAHMEN ================= */

    TEILNEHMERBEITRAG(
            "Teilnehmerbeiträge",
            FinanzTyp.EINNAHME,
            110
    ),

    PFAND(
            "Pfand",
            FinanzTyp.EINNAHME,
            120
    ),

    KJFP_ZUSCHUSS(
            "KJFP-Zuschuss",
            FinanzTyp.EINNAHME,
            130
    ),

    EIGENANTEIL(
            "Eigenanteil Verein",
            FinanzTyp.EINNAHME,
            150
    ),

    SONSTIGE_EINNAHMEN(
            "Sonstige Einnahmen",
            FinanzTyp.EINNAHME,
            160
    );

    private final String bezeichnung;

    private final FinanzTyp typ;

    /**
     * Reihenfolge in der Anzeige.
     */
    private final int sortierung;

    FinanzKategorie(
            String bezeichnung,
            FinanzTyp typ,
            int sortierung) {

        this.bezeichnung = bezeichnung;
        this.typ = typ;
        this.sortierung = sortierung;
    }

    public boolean isKosten() {
        return typ == FinanzTyp.KOSTEN;
    }

    public boolean isEinnahme() {
        return typ == FinanzTyp.EINNAHME;
    }
}