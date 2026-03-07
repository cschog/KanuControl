package com.kcserver.mapper;

import com.kcserver.dto.abrechnung.AbrechnungBuchungDTO;
import com.kcserver.dto.abrechnung.AbrechnungDetailDTO;
import com.kcserver.entity.Abrechnung;
import com.kcserver.entity.AbrechnungBuchung;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AbrechnungMapper {

    /* =========================================================
       ABRECHNUNG → DTO
       ========================================================= */

    public AbrechnungDetailDTO toDTO(Abrechnung a) {

        if (a == null) return null;

        AbrechnungDetailDTO dto = new AbrechnungDetailDTO();

        dto.setVeranstaltungId(
                a.getVeranstaltung() != null
                        ? a.getVeranstaltung().getId()
                        : null
        );

        dto.setStatus(a.getStatus());

        dto.setBuchungen(
                a.getBuchungen()
                        .stream()
                        .map(this::toDTO)
                        .collect(Collectors.toList())
        );

        return dto;
    }

    /* =========================================================
       BUCHUNG → DTO
       ========================================================= */

    public AbrechnungBuchungDTO toDTO(AbrechnungBuchung b) {

        if (b == null) return null;

        AbrechnungBuchungDTO dto = new AbrechnungBuchungDTO();

        dto.setId(b.getId());
        dto.setKategorie(b.getKategorie());
        dto.setBetrag(b.getBetrag());
        dto.setDatum(b.getDatum());
        dto.setBeschreibung(b.getBeschreibung());

        return dto;
    }
}