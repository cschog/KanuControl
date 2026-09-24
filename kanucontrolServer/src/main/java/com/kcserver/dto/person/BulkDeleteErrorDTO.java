package com.kcserver.dto.person;

public record BulkDeleteErrorDTO(
        Long id,
        String message
) {
}