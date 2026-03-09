package com.kcserver.service;

import com.kcserver.entity.KikZuschlag;
import com.kcserver.repository.KikZuschlagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KikZuschlagService {

    private final KikZuschlagRepository repository;

    public KikZuschlag findOptionalGueltigAm(LocalDate datum) {
        return repository.findGueltigAm(datum).orElse(null);
    }

    public KikZuschlag findRequiredGueltigAm(LocalDate datum) {
        return repository.findGueltigAm(datum)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Kein gültiger KiK-Zuschlag gefunden"
                        ));
    }
}