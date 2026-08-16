package com.kcserver.repository.fahrkosten;

import com.kcserver.entity.Reisekostenabrechnung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;

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

    @Query("""
    select coalesce(sum(r.gesamtBetrag), 0)
    from Reisekostenabrechnung r
    where r.veranstaltung.id = :veranstaltungId
""")
    BigDecimal sumGesamtBetragByVeranstaltung(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
    SELECT
        t.finanzGruppe.id,
        COALESCE(SUM(r.gesamtBetrag), 0)
    FROM Reisekostenabrechnung r
    JOIN Teilnehmer t
        ON t.person.id = r.fahrer.id
       AND t.veranstaltung.id = r.veranstaltung.id
    WHERE r.veranstaltung.id = :veranstaltungId
    GROUP BY t.finanzGruppe.id
""")
    List<Object[]> sumGesamtBetragByFinanzGruppeGrouped(
            @Param("veranstaltungId") Long veranstaltungId
    );

    @Query("""
    SELECT r
    FROM Reisekostenabrechnung r
    JOIN Teilnehmer t
        ON t.person.id = r.fahrer.id
       AND t.veranstaltung.id = r.veranstaltung.id
    WHERE r.veranstaltung.id = :veranstaltungId
      AND t.finanzGruppe.id = :finanzGruppeId
    ORDER BY r.abrechnungsdatum ASC, r.id ASC
""")
    List<Reisekostenabrechnung> findByFinanzGruppe(
            @Param("veranstaltungId") Long veranstaltungId,
            @Param("finanzGruppeId") Long finanzGruppeId
    );
}