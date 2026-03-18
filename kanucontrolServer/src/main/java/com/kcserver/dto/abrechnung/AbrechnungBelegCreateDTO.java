package com.kcserver.dto.abrechnung;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AbrechnungBelegCreateDTO {

    private String belegnummer;

    private String kuerzel;

    @NotNull
    private LocalDate datum;

    private String beschreibung;
}