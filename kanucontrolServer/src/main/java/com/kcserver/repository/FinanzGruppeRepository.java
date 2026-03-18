package com.kcserver.repository;

import com.kcserver.dto.finanz.FinanzGruppeDTO;
import com.kcserver.dto.finanz.FinanzGruppeOverviewDTO;
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
    SELECT new com.kcserver.dto.finanz.FinanzGruppeOverviewDTO(
        fg.id,
        fg.kuerzel,
        COUNT(DISTINCT t.id),
        (
            SELECT COUNT(b.id)
            FROM AbrechnungBeleg b
            WHERE b.finanzGruppe.id = fg.id
        )
    )
    FROM FinanzGruppe fg
    LEFT JOIN fg.teilnehmer t
    WHERE fg.veranstaltung.id = :veranstaltungId
    GROUP BY fg.id, fg.kuerzel
    ORDER BY fg.kuerzel
""")
    List<FinanzGruppeOverviewDTO> findOverviewByVeranstaltungId(Long veranstaltungId);

    @Query("""
    SELECT fg
    FROM FinanzGruppe fg
    LEFT JOIN FETCH fg.teilnehmer t
    LEFT JOIN FETCH t.person
    WHERE fg.id = :gruppeId
""")
    Optional<FinanzGruppe> findWithTeilnehmer(Long gruppeId);
}