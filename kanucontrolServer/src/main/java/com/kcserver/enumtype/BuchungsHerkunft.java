package com.kcserver.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BuchungsHerkunft {

    MANUELL(
            null,
            null
    ),

    TEILNEHMERBEITRAG(
            "AUTO-TN",
            "TN-Beiträge"
    ),

    FAHRTKOSTEN(
            "AUTO-RK",
            "Fahrtkosten"
    ),

    KJFP(
            "AUTO-KJFP",
            "KJFP-Zuschuss"
    );

    private final String belegnummer;
    private final String beschreibung;
}