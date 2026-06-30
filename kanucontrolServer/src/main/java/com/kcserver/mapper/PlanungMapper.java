package com.kcserver.mapper;

import com.kcserver.dto.planung.PlanungDetailDTO;
import com.kcserver.dto.planung.PlanungPositionDTO;
import com.kcserver.entity.Planung;
import com.kcserver.entity.PlanungPosition;
import org.springframework.stereotype.Component;

@Component
public class PlanungMapper {

    /* =========================================================
       ENTITY → DTO
       ========================================================= */

    public PlanungDetailDTO toDTO(
            Planung planung
    ) {

        if (planung == null) {
            return null;
        }

        PlanungDetailDTO dto = new PlanungDetailDTO();

        dto.setVeranstaltungId(
                planung.getVeranstaltung() != null
                        ? planung.getVeranstaltung().getId()
                        : null
        );

        dto.setEingereicht(
                planung.isEingereicht()
        );

        dto.setPositionen(
                planung.getPositionen()
                        .stream()
                        .map(this::toPositionDTO)
                        .toList()
        );

        return dto;
    }

    /* =========================================================
       POSITION → DTO
       ========================================================= */

    public PlanungPositionDTO toPositionDTO(
            PlanungPosition position
    ) {

        if (position == null) {
            return null;
        }

        PlanungPositionDTO dto = new PlanungPositionDTO();

        dto.setId(position.getId());

        dto.setKategorie(position.getKategorie());

        dto.setMenge(position.getMenge());
        dto.setEinheit(position.getEinheit());
        dto.setEinzelpreis(position.getEinzelpreis());

        dto.setBetrag(position.getBetrag());

        dto.setAutomatischBerechnet(
                position.isAutomatischBerechnet()
        );

        dto.setEditierbar(
                position.isEditierbar()
        );

        dto.setKommentar(
                position.getKommentar()
        );

        return dto;
    }
}