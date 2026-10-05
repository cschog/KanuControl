package com.kcserver.kjfp.dto.reisekosten;

import com.kcserver.core.dto.person.PersonRefDTO;

import java.util.List;

public record FahrtabschnittResponse(

        Long id,
        Integer reihenfolge,
        String beschreibung,
        String vonOrt,
        String nachOrt,
        Integer kilometer,
        boolean anhaenger,
        List<PersonRefDTO> mitfahrer

) {}
