package com.kcserver.exception;

public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }

    public static final String TEILNEHMER_NOT_IN_VERANSTALTUNG =
            "Teilnehmer gehört nicht zur angegebenen Veranstaltung";
}