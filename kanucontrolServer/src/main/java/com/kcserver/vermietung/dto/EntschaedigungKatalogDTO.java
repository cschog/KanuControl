package com.kcserver.vermietung.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record EntschaedigungKatalogDTO(Long id,Long version,
 @NotBlank @Size(max=250) String bezeichnung,
 @NotNull @DecimalMin(value="0",inclusive=false) @Digits(integer=8,fraction=2) BigDecimal einzelpreis,
 @Min(0) int sortierung,boolean aktiv) {}
