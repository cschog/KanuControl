package com.kcserver.dto.abrechnung;

import com.kcserver.enumtype.BuchungsHerkunft;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AbrechnungBelegDTO {

    private Long id;
    private BuchungsHerkunft herkunft;

    private String belegnummer;
    private LocalDate datum;
    private String beschreibung;

    private String kuerzel;

    private List<AbrechnungBuchungDTO> positionen;
}