package com.kcserver.kjfp.dto.zahlungsnachweis;

public record TeilnehmerOhneKontoDTO(
        Long teilnehmerId,
        String vorname,
        String nachname
) {
}
