package com.kcserver.enumtype;

import lombok.Getter;

@Getter
public enum VeranstaltungTyp {

    JEM(true, 6, 20),
    FM(true, 6, 20),

    BM(false, null, null),
    GV(false, null, null);

    private final boolean foerderfaehig;

    private final Integer mindestalter;
    private final Integer hoechstalter;

    VeranstaltungTyp(
            boolean foerderfaehig,
            Integer mindestalter,
            Integer hoechstalter
    ) {
        this.foerderfaehig = foerderfaehig;
        this.mindestalter = mindestalter;
        this.hoechstalter = hoechstalter;
    }
}