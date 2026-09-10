package com.kcserver.dto.zahlungsnachweis;

public record TeilnehmerOhneKontoDTO(
        Long teilnehmerId,
        String vorname,
        String nachname
) {
}
