package com.kcserver.core.dto.teilnehmer;

public record TeilnehmerKurzDTO(
        Long id,
        Long personId,
        String vorname,
        String nachname
) {}
