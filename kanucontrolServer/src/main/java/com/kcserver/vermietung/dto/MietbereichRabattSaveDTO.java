package com.kcserver.vermietung.dto;

import com.kcserver.vermietung.enumtype.Rabattart;
import com.kcserver.vermietung.enumtype.RabattwertTyp;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MietbereichRabattSaveDTO {

    @NotNull
    private Rabattart rabattart;

    @NotNull
    private RabattwertTyp rabattwertTyp;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal rabattwert;

    private boolean aktiv = true;
}