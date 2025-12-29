package com.kcserver.enumtype;

public enum Sex implements CodeEnum {

    MAENNLICH("M"),
    WEIBLICH("W"),
    DIVERS("D");

    private final String code;

    Sex(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}