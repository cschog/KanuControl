package com.kcserver.exception;

public class CsvReadException extends RuntimeException {

    public CsvReadException(String message, Throwable cause) {
        super(message, cause);
    }
}