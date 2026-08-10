package com.kcserver.repository.abrechnung;

import com.kcserver.entity.ZahlungsnachweisDokument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ZahlungsnachweisDokumentRepository
        extends JpaRepository<ZahlungsnachweisDokument, Long> {

    List<ZahlungsnachweisDokument>
    findByZahlungsnachweisIdOrderByReihenfolgeAsc(
            Long zahlungsnachweisId
    );

    Optional<ZahlungsnachweisDokument>
    findByIdAndZahlungsnachweisId(
            Long id,
            Long zahlungsnachweisId
    );

    void deleteByIdAndZahlungsnachweisId(
            Long id,
            Long zahlungsnachweisId
    );
}