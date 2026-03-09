package com.kcserver.mapper;

import com.kcserver.dto.foerder.FoerdersatzCreateUpdateDTO;
import com.kcserver.dto.foerder.FoerdersatzDTO;
import com.kcserver.entity.Foerdersatz;
import org.springframework.stereotype.Component;

@Component
public class FoerdersatzMapper {

    public FoerdersatzDTO toDTO(Foerdersatz entity) {

        FoerdersatzDTO dto = new FoerdersatzDTO();

        dto.setId(entity.getId());
        dto.setGueltigVon(entity.getGueltigVon());
        dto.setGueltigBis(entity.getGueltigBis());
        dto.setBetragProTeilnehmer(entity.getBetragProTeilnehmer());
        dto.setBeschluss(entity.getBeschluss());

        return dto;
    }

    public void updateEntity(
            FoerdersatzCreateUpdateDTO dto,
            Foerdersatz entity
    ) {
        entity.setGueltigVon(dto.getGueltigVon());
        entity.setGueltigBis(dto.getGueltigBis());
        entity.setBetragProTeilnehmer(dto.getBetragProTeilnehmer());
        entity.setBeschluss(dto.getBeschluss());
    }
}