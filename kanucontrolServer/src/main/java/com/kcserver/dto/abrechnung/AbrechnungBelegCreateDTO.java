package com.kcserver.dto.abrechnung;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AbrechnungBelegCreateDTO {

    @NotBlank
    private String kuerzel;

    @NotNull
    private LocalDate datum;

    /**
     * Optional: Händler/Lieferant
     */
    private String aussteller;

    /**
     * Optional: Rechnungs- oder Belegnummer des Ausstellers
     */
    private String externeBelegnummer;

    /**
     * Optionale Beschreibung
     */
    private String beschreibung;
}