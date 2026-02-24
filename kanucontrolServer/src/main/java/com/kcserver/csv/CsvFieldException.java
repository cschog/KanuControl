package com.kcserver.csv;

public class CsvFieldException extends RuntimeException {

    private final String field;
    private final String value;

    public CsvFieldException(String field, String value, String message) {
        super(message);
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}