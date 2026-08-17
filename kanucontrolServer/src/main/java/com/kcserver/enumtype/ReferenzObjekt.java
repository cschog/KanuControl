package com.kcserver.enumtype;
import lombok.Getter;

@Getter
public enum ReferenzObjekt {
    DIN_A4(
            "DIN A4",
            210.0,
            297.0
    ),

    DIN_A5(
            "DIN A5",
            148.0,
            210.0
    ),

    DIN_A6(
            "DIN A6",
            105.0,
            148.0
    ),

    DIN_A7(
            "DIN A7",
            74.0,
            105.0
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