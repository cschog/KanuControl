package com.kcserver.vermietung.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SortierungRequest(
        @NotEmpty
        List<@NotNull Long> ids
) {
}