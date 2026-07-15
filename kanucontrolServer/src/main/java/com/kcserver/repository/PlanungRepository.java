package com.kcserver.repository;

import com.kcserver.entity.Abrechnung;
import com.kcserver.entity.Planung;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface PlanungRepository extends JpaRepository<Planung, Long> {

    @EntityGraph(attributePaths = "veranstaltung")
    Optional<Planung> findByVeranstaltungId(Long veranstaltungId);

    @Query("""
        select p
        from Planung p
        left join fetch p.positionen
        where p.veranstaltung.id = :veranstaltungId
        """)
    Optional<Planung> findByVeranstaltungIdWithPositionen(Long veranstaltungId);

    @Query("""
        select distinct a
        from Abrechnung a
        left join fetch a.belege b
        left join fetch b.positionen
        where a.veranstaltung.id = :veranstaltungId
        """)
    Optional<Abrechnung> findByVeranstaltungIdWithBelegeAndPositionen(Long veranstaltungId);
}