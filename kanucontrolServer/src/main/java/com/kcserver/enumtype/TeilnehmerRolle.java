package com.kcserver.enumtype;

public enum TeilnehmerRolle implements CodeEnum {

    LEITER("L"),
    MITARBEITER("M");

    private final String code;

    TeilnehmerRolle(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}