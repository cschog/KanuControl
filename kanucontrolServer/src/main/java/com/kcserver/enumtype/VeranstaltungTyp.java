package com.kcserver.enumtype;

public enum VeranstaltungTyp implements CodeEnum {

    JUGENDERHOLUNGSMASSNAHME("JEM"),
    FREIZEITSMASSNAHME("FM"),
    BILDUNGSVERANSTALTUNG("B"),
    GROSSVERANSTALTUNG("G");

    private final String code;

    VeranstaltungTyp(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}