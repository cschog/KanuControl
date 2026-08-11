package com.kcserver.repository.abrechnung;

import com.kcserver.entity.ZahlungsnachweisDokument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZahlungsnachweisDokumentRepository
        extends JpaRepository<ZahlungsnachweisDokument, Long> {

    /**
     * Alle Dokumente eines Zahlungsnachweises in der definierten Reihenfolge.
     */
    List<ZahlungsnachweisDokument> findByZahlungsnachweisIdOrderByReihenfolgeAsc(
            Long zahlungsnachweisId
    );

    /**
     * Anzahl der Dokumente eines Zahlungsnachweises.
     */
    long countByZahlungsnachweisId(
            Long zahlungsnachweisId
    );

    /**
     * Höchste Reihenfolge eines Zahlungsnachweises.
     */
    ZahlungsnachweisDokument findTopByZahlungsnachweisIdOrderByReihenfolgeDesc(
            Long zahlungsnachweisId
    );
}