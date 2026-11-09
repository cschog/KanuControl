package com.kcserver.vermietung.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import com.kcserver.vermietung.entity.BuchungEntschaedigung.Anlass;
public record EntschaedigungPositionDTO(Long id,Long katalogId,
 @NotBlank @Size(max=250) String bezeichnung,@NotNull Anlass anlass,
 @NotNull @DecimalMin("1") @Digits(integer=6,fraction=0) BigDecimal anzahl,
 @NotNull @DecimalMin(value="0",inclusive=false) @Digits(integer=8,fraction=2) BigDecimal einzelpreis,
 BigDecimal katalogpreis,BigDecimal gesamtbetrag,
 @Size(max=1000) String bemerkung,@Size(max=1000) String preisbegruendung,@Min(0) int sortierung) {}
