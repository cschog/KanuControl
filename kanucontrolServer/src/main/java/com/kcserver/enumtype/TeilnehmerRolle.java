package com.kcserver.enumtype;

public enum TeilnehmerRolle implements CodeEnum {
    LEITER("L"),
    TEILNEHMER("T"),
    MITARBEITER("M");

    private final String code;

    TeilnehmerRolle(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}