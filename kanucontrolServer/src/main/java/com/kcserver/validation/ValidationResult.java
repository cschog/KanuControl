package com.kcserver.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {

    private final List<ValidationMessage> errors = new ArrayList<>();
    private final List<ValidationMessage> warnings = new ArrayList<>();

    public void addError(String message, String field) {
        errors.add(ValidationMessage.error(message, field));
    }

    public void addWarning(String message, String field) {
        warnings.add(ValidationMessage.warning(message, field));
    }

    public void addPdfWarning(String message, String field) {
        warnings.add(ValidationMessage.pdfWarning(message, field));
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public List<ValidationMessage> getErrors() {
        return List.copyOf(errors);
    }

    public List<ValidationMessage> getWarnings() {
        return List.copyOf(warnings);
    }
}