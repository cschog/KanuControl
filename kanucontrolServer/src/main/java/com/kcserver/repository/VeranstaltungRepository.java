package com.kcserver.repository;

import com.kcserver.entity.Veranstaltung;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

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

    @Query("""
    select v.leiter.id
    from Veranstaltung v
    where v.leiter.id in :personIds
""")
    Set<Long> findLeiterPersonIds(@Param("personIds") Collection<Long> personIds);
}