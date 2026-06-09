package com.kcserver.repository;

import com.kcserver.entity.Abrechnung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface AbrechnungRepository
        extends JpaRepository<Abrechnung, Long> {

    Optional<Abrechnung> findByVeranstaltungId(Long veranstaltungId);

    @Query("""
select distinct a
from Abrechnung a
left join fetch a.belege b
left join fetch b.positionen
where a.veranstaltung.id = :veranstaltungId
""")
    Optional<Abrechnung> findByVeranstaltungIdWithDetails(
            Long veranstaltungId
    );
}