package com.kcserver.dto.verpflegung;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
public class VerpflegungsmodellCreateUpdateDTO {

    @NotBlank
    private String bezeichnung;

    @NotNull
    @PositiveOrZero
    private BigDecimal preisProPersonUndTag;

    @Size(max = 500)
    private String bemerkung;

    private boolean aktiv;
}
