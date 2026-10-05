package com.kcserver.kjfp.dto.reisekosten;

import com.kcserver.kjfp.enumtype.CountryCode;

import java.util.List;

public record FahrtabschnittRequest(

        Long id,

        Integer reihenfolge,

        String beschreibung,

        String vonPlz,

        String vonOrt,

        CountryCode vonCountryCode,

        String nachPlz,

        String nachOrt,

        CountryCode nachCountryCode,

        Integer kilometer,

        boolean anhaenger,

        List<Long> mitfahrerIds

) {}
