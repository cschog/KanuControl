package com.kcserver.repository;
import com.kcserver.entity.FinanzGruppe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface FinanzGruppeRepository
        extends JpaRepository<FinanzGruppe, Long> {

    boolean existsByVeranstaltungIdAndKuerzel(
            Long veranstaltungId,
            String kuerzel
    );

    Optional<FinanzGruppe> findByVeranstaltungIdAndKuerzel(
            Long veranstaltungId,
            String kuerzel
    );

    List<FinanzGruppe> findByVeranstaltungIdOrderByKuerzel(
            Long veranstaltungId
    );
    @Query("""
SELECT DISTINCT fg
FROM FinanzGruppe fg
LEFT JOIN FETCH fg.teilnehmer t
LEFT JOIN FETCH t.person
WHERE fg.veranstaltung.id = :veranstaltungId
""")
    List<FinanzGruppe> findWithTeilnehmerByVeranstaltungId(Long veranstaltungId);

    @Query("""
    SELECT fg
    FROM FinanzGruppe fg
    LEFT JOIN FETCH fg.teilnehmer t
    LEFT JOIN FETCH t.person
    WHERE fg.id = :gruppeId
""")
    Optional<FinanzGruppe> findWithTeilnehmer(Long gruppeId);
}