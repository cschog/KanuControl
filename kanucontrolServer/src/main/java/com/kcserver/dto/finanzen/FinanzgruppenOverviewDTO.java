package com.kcserver.dto.finanzen;

import java.util.List;

public record FinanzgruppenOverviewDTO(
        List<FinanzGruppeOverviewDTO> gruppen,
        long teilnehmerGesamt
) {}