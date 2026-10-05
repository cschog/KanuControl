package com.kcserver.kjfp.dto.finanzen;

import jakarta.validation.constraints.NotBlank;

public record FinanzGruppeCreateDTO(
        @NotBlank String kuerzel
) {}