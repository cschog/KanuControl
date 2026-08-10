package com.kcserver.repository.abrechnung;

import com.kcserver.entity.BelegDokument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BelegDokumentRepository extends JpaRepository<BelegDokument, Long> {

    /**
     * Alle Dokumente eines Belegs in der definierten Reihenfolge.
     */
    List<BelegDokument> findByBelegIdOrderByReihenfolgeAsc(Long belegId);

    /**
     * Anzahl der Dokumente eines Belegs.
     */
    long countByBelegId(Long belegId);

    /**
     * Höchste Reihenfolge eines Belegs.
     */
    BelegDokument findTopByBelegIdOrderByReihenfolgeDesc(Long belegId);
}