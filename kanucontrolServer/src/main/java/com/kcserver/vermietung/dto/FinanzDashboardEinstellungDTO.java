package com.kcserver.vermietung.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record FinanzDashboardEinstellungDTO(
    @NotNull @DecimalMin("0.01") @Digits(integer=10, fraction=2) BigDecimal umsatzgrenze) {}
