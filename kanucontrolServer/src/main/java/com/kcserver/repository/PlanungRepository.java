package com.kcserver.repository;

import com.kcserver.entity.Planung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface PlanungRepository extends JpaRepository<Planung, Long> {

    Optional<Planung> findByVeranstaltungId(Long veranstaltungId);

    @Query("""
select p
from Planung p
left join fetch p.positionen
where p.veranstaltung.id = :veranstaltungId
""")
    Optional<Planung> findByVeranstaltungIdWithPositionen(
            Long veranstaltungId
    );
}