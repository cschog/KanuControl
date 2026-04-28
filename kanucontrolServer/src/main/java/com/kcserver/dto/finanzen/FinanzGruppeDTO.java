package com.kcserver.dto.finanzen;

import com.kcserver.dto.teilnehmer.TeilnehmerKurzDTO;

import java.util.List;

public record FinanzGruppeDTO(
        Long id,
        String kuerzel,
        List<TeilnehmerKurzDTO> teilnehmer
) {}