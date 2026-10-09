package com.kcserver.vermietung.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

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
}