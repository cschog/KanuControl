package com.kcserver.core.dto.person;

public record BulkDeleteErrorDTO(
        Long id,
        String message
) {
}