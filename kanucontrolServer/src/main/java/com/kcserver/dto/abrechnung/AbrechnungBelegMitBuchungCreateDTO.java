package com.kcserver.dto.abrechnung;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AbrechnungBelegMitBuchungCreateDTO {

    @Valid
    @NotNull
    private AbrechnungBelegCreateDTO beleg;

    @Valid
    @NotNull
    private AbrechnungBuchungCreateDTO buchung;
}