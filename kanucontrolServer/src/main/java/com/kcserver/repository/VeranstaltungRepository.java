package com.kcserver.repository;

import com.kcserver.entity.Veranstaltung;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VeranstaltungRepository extends
        JpaRepository<Veranstaltung, Long>,
        JpaSpecificationExecutor<Veranstaltung> {

    Optional<Veranstaltung> findByAktivTrue();

    @Query("""
        select v
        from Veranstaltung v
        left join fetch v.verein
        left join fetch v.leiter
        where v.id = :id
    """)
    Optional<Veranstaltung> findByIdWithRelations(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Veranstaltung v
           set v.aktiv = false
         where v.aktiv = true
    """)
    int unsetAktiveVeranstaltung();
}