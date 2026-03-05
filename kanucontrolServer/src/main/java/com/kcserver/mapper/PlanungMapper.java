package com.kcserver.mapper;

import com.kcserver.dto.planung.PlanungDetailDTO;
import com.kcserver.dto.planung.PlanungPositionDTO;
import com.kcserver.entity.Planung;
import com.kcserver.entity.PlanungPosition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlanungMapper {

    /* =========================================================
       ENTITY → DTO
       ========================================================= */

    public PlanungDetailDTO toDTO(Planung p) {

        if (p == null) return null;

        PlanungDetailDTO dto = new PlanungDetailDTO();

        dto.setVeranstaltungId(
                p.getVeranstaltung() != null
                        ? p.getVeranstaltung().getId()
                        : null
        );

        List<PlanungPositionDTO> positionen =
                p.getPositionen()
                        .stream()
                        .map(this::toPositionDTO)
                        .collect(Collectors.toList());

        dto.setPositionen(positionen);

        dto.setEingereicht(p.isEingereicht());

        return dto;
    }

    /* =========================================================
       POSITION → DTO
       ========================================================= */

    public PlanungPositionDTO toPositionDTO(PlanungPosition pos) {

        if (pos == null) return null;

        PlanungPositionDTO dto = new PlanungPositionDTO();

        dto.setId(pos.getId());
        dto.setKategorie(pos.getKategorie());
        dto.setBetrag(pos.getBetrag());
        dto.setKommentar(pos.getKommentar());

        return dto;
    }
}