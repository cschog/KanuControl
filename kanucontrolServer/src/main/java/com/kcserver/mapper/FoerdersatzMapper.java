package com.kcserver.mapper;

import com.kcserver.dto.foerder.FoerdersatzCreateUpdateDTO;
import com.kcserver.dto.foerder.FoerdersatzDTO;
import com.kcserver.entity.Foerdersatz;
import org.springframework.stereotype.Component;

@Component
public class FoerdersatzMapper {

    /* =========================================================
       ENTITY → DTO
       ========================================================= */

    public FoerdersatzDTO toDTO(Foerdersatz entity) {

        if (entity == null) return null;

        FoerdersatzDTO dto = new FoerdersatzDTO();

        dto.setId(entity.getId());
        dto.setTyp(entity.getTyp());
        dto.setGueltigVon(entity.getGueltigVon());
        dto.setGueltigBis(entity.getGueltigBis());
        dto.setFoerdersatz(entity.getFoerdersatz());
        dto.setBeschluss(entity.getBeschluss());

        return dto;
    }

    /* =========================================================
       CREATE/UPDATE DTO → ENTITY
       ========================================================= */

    public void applyToEntity(
            FoerdersatzCreateUpdateDTO dto,
            Foerdersatz entity
    ) {

        entity.setTyp(dto.getTyp());
        entity.setGueltigVon(dto.getGueltigVon());
        entity.setGueltigBis(dto.getGueltigBis());
        entity.setFoerdersatz(dto.getFoerdersatz());
        entity.setBeschluss(dto.getBeschluss());
    }
}