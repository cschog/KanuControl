package com.kcserver.enumtype;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TeilnehmerRolle implements CodeEnum {

    LEITER("L"),
    MITARBEITER("M");

    private final String code;

    TeilnehmerRolle(String code) {
        this.code = code;
    }

    /* DB + JSON */
    @Override
    @JsonValue
    public String getCode() {
        return code;
    }

    /* JSON → Enum */
    @JsonCreator
    public static TeilnehmerRolle fromCode(String value) {
        if (value == null) return null;

        for (TeilnehmerRolle r : values()) {
            if (r.code.equalsIgnoreCase(value)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown TeilnehmerRolle code: " + value);
    }
}