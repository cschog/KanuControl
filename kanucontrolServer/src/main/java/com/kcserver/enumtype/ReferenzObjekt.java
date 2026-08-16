package com.kcserver.enumtype;

import lombok.Getter;

@Getter
public enum ReferenzObjekt {

    EIN_EURO_MUENZE(
            "1-Euro-Münze",
            23.25,
            23.25
    ),

    ZWEI_EURO_MUENZE(
            "2-Euro-Münze",
            25.75,
            25.75
    ),

    EINKAUFSCHIP(
            "Einkaufswagen-Chip",
            23.0,
            23.0
    ),

    EC_KARTE(
            "EC-/Kreditkarte",
            85.60,
            53.98
    ),

    DIN_A4(
            "DIN A4",
            210.0,
            297.0
    ),

    DIN_A5(
            "DIN A5",
            148.0,
            210.0
    );

    private final String bezeichnung;
    private final double breiteMm;
    private final double hoeheMm;

    ReferenzObjekt(
            String bezeichnung,
            double breiteMm,
            double hoeheMm
    ) {
        this.bezeichnung = bezeichnung;
        this.breiteMm = breiteMm;
        this.hoeheMm = hoeheMm;
    }
}