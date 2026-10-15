package com.kcserver.vermietung.service;

import com.kcserver.core.entity.Verein;
import com.kcserver.core.repository.VereinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class VermietungVereinService {

    private final VereinRepository vereinRepository;
    public void setHausverein(Long vereinId) {

        // 1. Prüfen, ob der Verein existiert
        if (!vereinRepository.existsById(vereinId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Verein nicht gefunden."
            );
        }

        // 2. Bisherigen Hausverein deaktivieren
        vereinRepository.unsetHausverein();

        // 3. Verein erneut laden
        Verein neuerHausverein = vereinRepository.findById(vereinId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Verein nicht gefunden."
                ));

        // 4. Neuen Hausverein aktivieren
        neuerHausverein.setHausverein(true);
    }
}