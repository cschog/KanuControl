package com.kcserver.dto.abrechnung;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AbrechnungBelegDTO {

    private Long id;

    private String belegnummer;
    private LocalDate datum;
    private String beschreibung;

    private List<AbrechnungBuchungDTO> positionen;
}