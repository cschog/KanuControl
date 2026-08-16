package com.kcserver.repository.abrechnung;

import com.kcserver.entity.Dokument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DokumentRepository extends JpaRepository<Dokument, Long> {

    /**
     * Alle Dokumente eines Belegs in der definierten Reihenfolge.
     */
    List<Dokument> findByBelegIdOrderByReihenfolgeAsc(Long belegId);

    /**
     * Anzahl der Dokumente eines Belegs.
     */
    long countByBelegId(Long belegId);

    /**
     * Höchste Reihenfolge eines Belegs.
     */
    Dokument findTopByBelegIdOrderByReihenfolgeDesc(Long belegId);

    /**
     * Alle Dokumente eines Zahlungsnachweises
     * in der definierten Reihenfolge.
     */
    List<Dokument> findByZahlungsnachweisIdOrderByReihenfolgeAsc(
            Long zahlungsnachweisId
    );

    /**
     * Anzahl der Dokumente eines Zahlungsnachweises.
     */
    long countByZahlungsnachweisId(Long zahlungsnachweisId);

    /**
     * Höchste Reihenfolge eines Zahlungsnachweises.
     */
    Dokument findTopByZahlungsnachweisIdOrderByReihenfolgeDesc(
            Long zahlungsnachweisId
    );
}