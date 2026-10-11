package com.kcserver.vermietung.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ZaehlerstandDTO {

    private Long id;

    private Long zaehlerId;

    @NotNull
    private LocalDateTime ablesedatum;

    @NotNull
    @DecimalMin(value = "0.000")
    private BigDecimal zaehlerstand;

    @Size(max = 1000)
    private String bemerkung;
}