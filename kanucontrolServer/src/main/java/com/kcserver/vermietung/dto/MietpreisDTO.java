package com.kcserver.vermietung.dto;

import com.kcserver.vermietung.enumtype.Buchungsquelle;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MietpreisDTO {

    private Long id;

    @NotNull(message = "Der Mietbereich muss angegeben werden.")
    private Long mietbereichId;

    private String mietbereichBezeichnung;

    @NotNull(message = "Die Buchungsquelle muss angegeben werden.")
    private Buchungsquelle buchungsquelle;

    @NotNull(message = "Der Gültigkeitsbeginn muss angegeben werden.")
    private LocalDate gueltigAb;

    @NotNull(message = "Der Preis muss angegeben werden.")
    @DecimalMin(value = "0.00", inclusive = true,
            message = "Der Preis darf nicht negativ sein.")
    private BigDecimal preis;

    @Size(max = 1000, message = "Die Bemerkung darf höchstens 1000 Zeichen lang sein.")
    private String bemerkung;
}