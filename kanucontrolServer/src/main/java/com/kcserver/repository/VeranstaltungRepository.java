package com.kcserver.repository;

import com.kcserver.entity.Veranstaltung;
import com.kcserver.entity.Verein;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VeranstaltungRepository extends JpaRepository<Veranstaltung, Long> {

    /* =========================================================
       BASIC QUERIES
       ========================================================= */

    List<Veranstaltung> findByVerein(Verein verein);

    /* =========================================================
       AKTIVE VERANSTALTUNG
       ========================================================= */

    Optional<Veranstaltung> findByAktivTrue();

    boolean existsByAktivTrue();

    /* =========================================================
       VALIDIERUNGEN / FACHLICHE HILFSMETHODEN
       ========================================================= */

    boolean existsByNameAndVerein(String name, Verein verein);
}