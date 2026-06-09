package com.kcserver.service.pdf;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ValidationResult {

    private boolean valid;
    private List<String> messages;

    public static ValidationResult valid() {
        return new ValidationResult(true, List.of());
    }

    public static ValidationResult invalid(List<String> messages) {
        return new ValidationResult(false, messages);
    }
}
