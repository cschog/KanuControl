package com.kcserver.enumtype;

public enum FinanzKategorie {

    /* ================= PLAN + IST KOSTEN ================= */

    UNTERKUNFT(FinanzTyp.KOSTEN),
    VERPFLEGUNG(FinanzTyp.KOSTEN),
    HONORARE(FinanzTyp.KOSTEN),
    FAHRTKOSTEN(FinanzTyp.KOSTEN),
    VERBRAUCHSMATERIAL(FinanzTyp.KOSTEN),
    KULTUR(FinanzTyp.KOSTEN),
    MIETE(FinanzTyp.KOSTEN),
    SONSTIGE_KOSTEN(FinanzTyp.KOSTEN),

    /* ================= EINNAHMEN ================= */

    TEILNEHMERBEITRAG(FinanzTyp.EINNAHME),
    PFAND(FinanzTyp.EINNAHME),
    KJFP_ZUSCHUSS(FinanzTyp.EINNAHME),
    SONSTIGE_EINNAHMEN(FinanzTyp.EINNAHME);

    private final FinanzTyp typ;

    FinanzKategorie(FinanzTyp typ) {
        this.typ = typ;
    }

    public boolean isKosten() {
        return typ == FinanzTyp.KOSTEN;
    }

    public boolean isEinnahme() {
        return typ == FinanzTyp.EINNAHME;
    }

    public FinanzTyp getTyp() {
        return typ;
    }
}