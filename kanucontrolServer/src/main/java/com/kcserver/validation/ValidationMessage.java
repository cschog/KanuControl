package com.kcserver.validation;

public record ValidationMessage(
        ValidationSeverity severity,
        String message,
        String field,
        boolean editableInPdf
) {

    public static ValidationMessage error(String message, String field) {
        return new ValidationMessage(
                ValidationSeverity.ERROR,
                message,
                field,
                false
        );
    }

    public static ValidationMessage warning(String message, String field) {
        return new ValidationMessage(
                ValidationSeverity.WARNING,
                message,
                field,
                false
        );
    }

    public static ValidationMessage pdfWarning(
            String message,
            String field
    ) {
        return new ValidationMessage(
                ValidationSeverity.WARNING,
                message,
                field,
                true
        );
    }
}