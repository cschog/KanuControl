package com.kcserver.dto.finanz;

import jakarta.validation.constraints.NotBlank;

public record FinanzGruppeCreateDTO(
        @NotBlank String kuerzel
) {}