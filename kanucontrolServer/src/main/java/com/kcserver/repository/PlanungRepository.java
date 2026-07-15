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
    select distinct p
    from Planung p
    join fetch p.veranstaltung v
    join fetch v.verein
    left join fetch v.leiter
    left join fetch v.unterkunftsart
    left join fetch v.verpflegungsmodell
    left join fetch p.positionen
    where v.id = :veranstaltungId
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