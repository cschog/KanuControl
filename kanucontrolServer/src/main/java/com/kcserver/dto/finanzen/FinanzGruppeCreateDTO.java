package com.kcserver.dto.finanzen;

import jakarta.validation.constraints.NotBlank;

public record FinanzGruppeCreateDTO(
        @NotBlank String kuerzel
) {}