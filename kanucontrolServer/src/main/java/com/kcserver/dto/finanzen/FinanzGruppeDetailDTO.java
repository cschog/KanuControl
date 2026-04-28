package com.kcserver.dto.finanzen;

import com.kcserver.dto.teilnehmer.TeilnehmerKurzDTO;

import java.util.List;

public record FinanzGruppeDetailDTO(
        Long id,
        String kuerzel,
        List<TeilnehmerKurzDTO> teilnehmer
) {}