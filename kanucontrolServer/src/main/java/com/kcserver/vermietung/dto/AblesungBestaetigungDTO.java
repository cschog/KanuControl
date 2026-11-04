package com.kcserver.vermietung.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Zustimmung gilt nur für die angezeigten Werte und den geprüften Buchungszeitraum. */
public record AblesungBestaetigungDTO(
        @NotNull Long zaehlerId,
        @NotNull LocalDateTime anreise,
        @NotNull LocalDateTime abreise,
        @NotNull LocalDateTime anfangsablesung,
        @NotNull LocalDateTime endablesung,
        @NotNull BigDecimal anfangsstand,
        @NotNull BigDecimal endstand
) {}
