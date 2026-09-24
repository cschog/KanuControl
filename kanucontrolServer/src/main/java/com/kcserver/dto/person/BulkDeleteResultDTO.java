package com.kcserver.dto.person;

import java.util.List;

public record BulkDeleteResultDTO(
        List<Long> deletedIds,
        List<BulkDeleteErrorDTO> errors
) {
}