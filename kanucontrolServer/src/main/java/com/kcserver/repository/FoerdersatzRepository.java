package com.kcserver.repository;

import com.kcserver.entity.Foerdersatz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface FoerdersatzRepository
        extends JpaRepository<Foerdersatz, Long> {

    /* =========================================================
       Aktuellen Satz für ein Datum holen
       ========================================================= */

    Optional<Foerdersatz>
    findFirstByGueltigVonLessThanEqualAndGueltigBisGreaterThanEqual(
            LocalDate datum1,
            LocalDate datum2
    );

    /* =========================================================
       Prüfen, ob sich Zeitraum überschneidet
       ========================================================= */

    boolean existsByGueltigVonLessThanEqualAndGueltigBisGreaterThanEqual(
            LocalDate bis,
            LocalDate von
    );
}