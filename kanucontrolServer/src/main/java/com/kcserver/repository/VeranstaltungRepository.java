package com.kcserver.repository;

import com.kcserver.entity.Veranstaltung;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VeranstaltungRepository extends
        JpaRepository<Veranstaltung, Long>,
        JpaSpecificationExecutor<Veranstaltung> {

    Optional<Veranstaltung> findByAktivTrue();

    Optional<Veranstaltung> findTopByOrderByBeginnDatumDesc();

    @Query("""
select v
from Veranstaltung v
left join fetch v.verein
left join fetch v.leiter
left join fetch v.unterkunftsart
left join fetch v.verpflegungsmodell
left join fetch v.beitragsstruktur
where v.id = :id
""")
    Optional<Veranstaltung> findByIdWithRelations(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Veranstaltung v
           set v.aktiv = false
         where v.aktiv = true
    """)
    int unsetAktiveVeranstaltung();

    @Query("""
select v
from Veranstaltung v
join fetch v.verein
where v.id = :id
""")
    Optional<Veranstaltung> findByIdWithVerein(Long id);

    Optional<Veranstaltung>
    findTopByOrderByBeginnDatumDescBeginnZeitDesc();

    Optional<Veranstaltung>
    findTopByIdNotOrderByBeginnDatumDescBeginnZeitDesc(Long id);

    boolean existsByVereinId(Long vereinId);
}