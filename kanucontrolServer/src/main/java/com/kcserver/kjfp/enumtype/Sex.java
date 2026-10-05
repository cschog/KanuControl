package com.kcserver.kjfp.enumtype;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Sex implements CodeEnum {

    MAENNLICH("M"),
    WEIBLICH("W"),
    DIVERS("D");

    private final String code;

    Sex(String code) {
        this.code = code;
    }

    /** 🔹 JSON & DB → "M" / "W" / "D" */
    @JsonValue
    @Override
    public String getCode() {
        return code;
    }

    /** 🔹 JSON → Enum */
    @JsonCreator
    public static Sex fromCode(String value) {
        if (value == null) return null;

        for (Sex s : values()) {
            if (s.code.equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown Sex code: " + value);
    }
}