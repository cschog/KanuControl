package com.kcserver.repository;

import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.TeilnehmerRolle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeilnehmerRepository extends JpaRepository<Teilnehmer, Long> {

    long countByVeranstaltungId(Long veranstaltungId);

    @Query("""
    select count(t) > 0
    from Teilnehmer t
    where t.veranstaltung.id = :veranstaltungId
      and (t.rolle is null or t.rolle <> :leiter)
""")
    boolean existsNichtLeiterTeilnehmer(
            @Param("veranstaltungId") Long veranstaltungId,
            @Param("leiter") TeilnehmerRolle leiter
    );

    /* =========================
       LISTEN (Paging)
       ========================= */

    Page<Teilnehmer> findByVeranstaltung(
            Veranstaltung veranstaltung,
            Pageable pageable
    );

    Page<Teilnehmer>    findByVeranstaltungAndRolle(
            Veranstaltung veranstaltung,
            TeilnehmerRolle rolle,
            Pageable pageable
    );

    /* =========================
       AVAILABLE (Paging)
       ========================= */

    @Query("""
select distinct p
from Person p
left join p.mitgliedschaften m
left join m.verein v
where p.id not in (
    select t.person.id
    from Teilnehmer t
    where t.veranstaltung.id = :veranstaltungId
)
and (:name is null or lower(p.name) like lower(concat('%', cast(:name as string), '%')))
and (:vorname is null or lower(p.vorname) like lower(concat('%', cast(:vorname as string), '%')))
and (:verein is null or lower(v.abk) like lower(concat('%', cast(:verein as string), '%')))
""")
    Page<Person> findAvailablePersonsFiltered(
            @Param("veranstaltungId") Long veranstaltungId,
            @Param("name") String name,
            @Param("vorname") String vorname,
            @Param("verein") String verein,
            Pageable pageable
    );


    @Query("""
select distinct t
from Teilnehmer t
join fetch t.person p
left join fetch p.mitgliedschaften m
left join fetch m.verein
where t.veranstaltung.id = :veranstaltungId
""")
    List<Teilnehmer> findByVeranstaltungWithPerson(Long veranstaltungId);

    /* =========================
       FACHLICHE CHECKS / LOGIK
       ========================= */

    Optional<Teilnehmer> findByVeranstaltungAndPerson(
            Veranstaltung veranstaltung,
            Person person
    );

    Optional<Teilnehmer> findByVeranstaltungAndRolle(
            Veranstaltung veranstaltung,
            TeilnehmerRolle rolle
    );

    long countByVeranstaltungIdAndRolle(Long veranstaltungId, TeilnehmerRolle rolle);

    long countByVeranstaltungIdAndRolleIsNull(Long veranstaltungId);

    /* =========================
       DELETE BULK
       ========================= */
    /* =========================================================
       BULK DELETE (ohne Leiter)
       ========================================================= */

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from Teilnehmer t
         where t.veranstaltung.id = :veranstaltungId
           and t.person.id in :personIds
    """)
    void deleteByVeranstaltungIdAndPersonIds(
            @Param("veranstaltungId") Long veranstaltungId,
            @Param("personIds") List<Long> personIds
    );

    @Modifying
    @Query("""
    delete from Teilnehmer t
     where t.veranstaltung.id = :veranstaltungId
""")
    void deleteAllByVeranstaltungId(@Param("veranstaltungId") Long veranstaltungId);

    /* =========================
       TEST / TECHNISCH
       ========================= */

    Optional<Teilnehmer> findByVeranstaltungIdAndPersonId(
            Long veranstaltungId,
            Long personId
    );

    /* =========================
       PERFORMANCE (UI)
       ========================= */

    @Query("""
        select t from Teilnehmer t
        join fetch t.person
        where t.veranstaltung = :veranstaltung
    """)
    Page<Teilnehmer> findWithPersonByVeranstaltung(
            @Param("veranstaltung") Veranstaltung veranstaltung,
            Pageable pageable
    );

    /* =========================
       STATISTIK / BERECHNUNG
       ========================= */

    List<Teilnehmer> findAllByVeranstaltung(
            Veranstaltung veranstaltung
    );
}