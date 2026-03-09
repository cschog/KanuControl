package com.kcserver.mapper;

import com.kcserver.dto.finanz.FinanzGruppeDTO;
import com.kcserver.entity.FinanzGruppe;
import org.springframework.stereotype.Component;

@Component
public class FinanzGruppeMapper {

    public FinanzGruppeDTO toDTO(FinanzGruppe g) {
        return new FinanzGruppeDTO(
                g.getId(),
                g.getKuerzel()
        );
    }
}