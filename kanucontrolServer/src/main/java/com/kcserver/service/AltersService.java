package com.kcserver.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class AltersService {

    public Integer berechneAlterBeiBeginn(
            LocalDate geburtsdatum,
            LocalDate veranstaltungsbeginn
    ) {

        if (geburtsdatum == null || veranstaltungsbeginn == null) {
            return null;
        }

        if (veranstaltungsbeginn.isBefore(geburtsdatum)) {
            return 0;
        }

        return Period.between(
                geburtsdatum,
                veranstaltungsbeginn
        ).getYears();
    }
}
