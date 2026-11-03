package com.kcserver.vermietung.dto;
import jakarta.validation.constraints.*;
import java.util.Set;
public record AirbnbExportDTO(boolean aktiv, @NotNull @Size(max = 200) Set<@NotNull @Positive Long> mietbereichIds,
        @NotNull @PositiveOrZero Long version, boolean linkVorhanden, String feedPfad) {}
