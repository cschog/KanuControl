package com.kcserver.repository;

import com.kcserver.entity.Reisekostenabrechnung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReisekostenabrechnungRepository
        extends JpaRepository<Reisekostenabrechnung, Long> {

    List<Reisekostenabrechnung> findByVeranstaltungId(
            Long veranstaltungId
    );

    @Query("""
select count(r) > 0
from Reisekostenabrechnung r
where r.veranstaltung.id = :veranstaltungId
and (
    r.fahrer.id = :personId
    or exists (
        select 1
        from FahrtabschnittMitfahrer m
        where m.fahrtabschnitt.abrechnung = r
        and m.person.id = :personId
    )
)
and (:abrechnungId is null or r.id <> :abrechnungId)
""")
    boolean isPersonBereitsFahrzeugZugeordnet(
            Long veranstaltungId,
            Long personId,
            Long abrechnungId
    );
    boolean existsByFahrerId(Long personId);

    @Query("""
select count(r) > 0
from Reisekostenabrechnung r
where r.veranstaltung.id = :veranstaltungId
and (
    r.fahrer.id = :personId
    or exists (
        select 1
        from FahrtabschnittMitfahrer m
        where m.fahrtabschnitt.abrechnung = r
        and m.person.id = :personId
    )
)
""")
    boolean existsByVeranstaltungAndPersonVerwendet(
            @Param("veranstaltungId") Long veranstaltungId,
            @Param("personId") Long personId
    );

}