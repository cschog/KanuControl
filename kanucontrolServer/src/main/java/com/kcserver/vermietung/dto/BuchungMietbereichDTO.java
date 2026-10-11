package com.kcserver.vermietung.dto;

import com.kcserver.vermietung.enumtype.Berechnungsart;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BuchungMietbereichDTO {

    @NotNull
    private Long mietbereichId;

    @NotNull
    @Min(1)
    private Integer anzahl = 1;

    private String mietbereichBezeichnung;
    private String mengeneinheit;

    /** Bei der Anmeldung vereinbarter Einzelpreis */
    private BigDecimal vereinbarterPreis;

    /** Bei der Anmeldung gültige Berechnungsart */
    private Berechnungsart berechnungsart;
}