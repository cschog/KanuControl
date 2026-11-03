package com.kcserver.vermietung.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record SperrzeitDTO(Long id, Long version, @NotNull LocalDateTime von, @NotNull LocalDateTime bis,
        boolean ganztaegig, @NotBlank @Size(max = 500) String grund) {}
