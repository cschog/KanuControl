package com.kcserver.vermietung.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
public record KautionsverrechnungDTO(
        @NotNull @DecimalMin("0.01") @Digits(integer=10, fraction=2) BigDecimal betrag,
        @NotNull LocalDate datum,
        List<@Valid @NotNull AblesungBestaetigungDTO> ablesungen) {}
