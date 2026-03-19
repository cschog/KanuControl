package com.kcserver.dto.finanz;

import com.kcserver.dto.teilnehmer.TeilnehmerKurzDTO;

import java.util.List;

public record FinanzGruppeOverviewDTO(
        Long id,
        String kuerzel,
        List<TeilnehmerKurzDTO> teilnehmer,
        long belegCount
) {}