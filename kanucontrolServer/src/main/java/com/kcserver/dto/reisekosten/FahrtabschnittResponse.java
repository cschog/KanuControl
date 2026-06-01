package com.kcserver.dto.reisekosten;

import com.kcserver.dto.person.PersonRefDTO;

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
