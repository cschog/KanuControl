package com.kcserver.service;

import com.kcserver.entity.Foerdersatz;
import com.kcserver.repository.FoerdersatzRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FoerdersatzService {

    private final FoerdersatzRepository repository;

    /* =========================================================
       CREATE
       ========================================================= */

    @Transactional
    public Foerdersatz create(Foerdersatz satz) {

        if (satz.getGueltigVon().isAfter(satz.getGueltigBis())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "gueltigVon darf nicht nach gueltigBis liegen"
            );
        }

        boolean overlap =
                repository.existsByGueltigVonLessThanEqualAndGueltigBisGreaterThanEqual(
                        satz.getGueltigBis(),
                        satz.getGueltigVon()
                );

        if (overlap) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Zeitraum überschneidet sich mit bestehendem Fördersatz"
            );
        }

        return repository.save(satz);
    }

    /* =========================================================
       Aktuellen Satz ermitteln
       ========================================================= */

    @Transactional(readOnly = true)
    public Foerdersatz getSatzFuerDatum(LocalDate datum) {

        return repository
                .findFirstByGueltigVonLessThanEqualAndGueltigBisGreaterThanEqual(
                        datum,
                        datum
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Kein gültiger Fördersatz gefunden"
                        )
                );
    }
}