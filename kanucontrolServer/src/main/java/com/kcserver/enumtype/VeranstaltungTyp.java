package com.kcserver.enumtype;

public enum VeranstaltungTyp implements CodeEnum {

    JUGENDERHOLUNGSMASSNAHME("JEM"),
    FREIZEITSMASSNAHME("FM"),
    BILDUNGSVERANSTALTUNG("BV"),
    GROSSVERANSTALTUNG("GV");

    private final String code;

    VeranstaltungTyp(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}