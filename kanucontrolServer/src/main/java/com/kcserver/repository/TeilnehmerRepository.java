package com.kcserver.repository;

import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.TeilnehmerRolle;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public interface TeilnehmerRepository extends JpaRepository<Teilnehmer, Long>,
        JpaSpecificationExecutor<Teilnehmer> {

    Page<Teilnehmer> findByVeranstaltungId(Long veranstaltungId, Pageable pageable);

    /* =========================================================
       SEARCH (UI LIST)
       ========================================================= */
    @Query("""
    SELECT t
    FROM Teilnehmer t
    JOIN t.person p
    WHERE t.veranstaltung.id = :veranstaltungId
    AND (
        :search IS NULL
        OR :search = ''
        OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(p.vorname) LIKE LOWER(CONCAT('%', :search, '%'))
    )
    ORDER BY p.name, p.vorname
    """)
    List<Teilnehmer> search(Long veranstaltungId, String search);

    /* =========================================================
       SEARCH REF (Autocomplete)
       ========================================================= */
    @Query("""
    SELECT DISTINCT t
    FROM Teilnehmer t
    JOIN FETCH t.person p
    LEFT JOIN FETCH p.mitgliedschaften m
    LEFT JOIN FETCH m.verein
    WHERE t.veranstaltung.id = :veranstaltungId
    AND (
        :search IS NULL
        OR :search = ''
        OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(p.vorname) LIKE LOWER(CONCAT('%', :search, '%'))
    )
    ORDER BY p.name, p.vorname
    """)
    List<Teilnehmer> searchRef(Long veranstaltungId, String search);

    /* =========================================================
       AVAILABLE
       ========================================================= */

    @Query("""
SELECT DISTINCT p
FROM Person p
LEFT JOIN p.mitgliedschaften m
LEFT JOIN m.verein v
WHERE
    (:verein IS NULL OR :verein = ''
        OR LOWER(v.abk) LIKE LOWER(CONCAT('%', :verein, '%'))
    )
AND NOT EXISTS (
    SELECT 1 FROM Teilnehmer t
    WHERE t.person = p
    AND t.veranstaltung.id = :veranstaltungId
)
AND (
    (:search IS NULL OR :search = '')
    OR (
        LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(p.vorname) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(CONCAT(p.vorname, ' ', p.name)) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(CONCAT(p.name, ' ', p.vorname)) LIKE LOWER(CONCAT('%', :search, '%'))
    )
)
""")
    Page<Person> findAvailable(
            @Param("veranstaltungId") Long veranstaltungId,
            @Param("search") String search,
            @Param("verein") String verein,
            Pageable pageable
    );


    @Query("""
SELECT p
FROM Person p
WHERE (
    :search IS NULL OR :search = ''
    OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
    OR LOWER(p.vorname) LIKE LOWER(CONCAT('%', :search, '%'))
    OR LOWER(CONCAT(p.vorname, ' ', p.name)) LIKE LOWER(CONCAT('%', :search, '%'))
    OR LOWER(CONCAT(p.name, ' ', p.vorname)) LIKE LOWER(CONCAT('%', :search, '%'))
)
AND NOT EXISTS (
    SELECT 1 FROM Teilnehmer t
    WHERE t.person = p
    AND t.veranstaltung.id = :veranstaltungId
)
ORDER BY p.name, p.vorname
""")
    List<Person> findAvailable(Long veranstaltungId, String search);

    /* =========================================================
       ASSIGNED
       ========================================================= */
    @Query("""
SELECT t
FROM Teilnehmer t
JOIN t.person p
LEFT JOIN p.mitgliedschaften m
LEFT JOIN m.verein v
WHERE t.veranstaltung.id = :veranstaltungId
AND (
    :search IS NULL OR :search = ''
    OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
    OR LOWER(p.vorname) LIKE LOWER(CONCAT('%', :search, '%'))
    OR LOWER(CONCAT(p.vorname, ' ', p.name)) LIKE LOWER(CONCAT('%', :search, '%'))
    OR LOWER(CONCAT(p.name, ' ', p.vorname)) LIKE LOWER(CONCAT('%', :search, '%'))
)
AND (
    :verein IS NULL OR :verein = ''
    OR LOWER(v.abk) LIKE LOWER(CONCAT('%', :verein, '%'))
)
ORDER BY p.name, p.vorname, t.id
""")
    Page<Teilnehmer> findAssignedWithPerson(
            Long veranstaltungId,
            String search,
            String verein,
            Pageable pageable
    );


    @Query("""
    SELECT t
    FROM Teilnehmer t
    JOIN FETCH t.person p
    WHERE t.veranstaltung.id = :veranstaltungId
    ORDER BY p.name, p.vorname
    """)
    List<Teilnehmer> findAllWithPerson(Long veranstaltungId);

    /* =========================================================
       BUSINESS
       ========================================================= */
    boolean existsByVeranstaltungAndPerson(Veranstaltung veranstaltung, Person person);

    Optional<Teilnehmer> findByVeranstaltungAndPerson(Veranstaltung veranstaltung, Person person);

    Optional<Teilnehmer> findByVeranstaltungAndRolle(Veranstaltung veranstaltung, TeilnehmerRolle rolle);

    Optional<Teilnehmer> findByVeranstaltungIdAndPersonId(Long veranstaltungId, Long personId);

    /* =========================================================
       DELETE
       ========================================================= */
    @Modifying
    @Query("""
        delete from Teilnehmer t
         where t.veranstaltung.id = :veranstaltungId
           and t.person.id in :personIds
    """)
    void deleteByVeranstaltungIdAndPersonIds(Long veranstaltungId, List<Long> personIds);

    List<Teilnehmer> findByVeranstaltungIdAndFinanzGruppeIsNull(Long veranstaltungId);

    /* =========================================================
   COUNTS
   ========================================================= */
    long countByVeranstaltungId(Long veranstaltungId);

    /* =========================================================
   FIND SINGLE
   ========================================================= */
    Optional<Teilnehmer> findByIdAndVeranstaltung_Id(Long id, Long veranstaltungId);

    /* =========================================================
   FINANZ CHECK
   ========================================================= */
    boolean existsByFinanzGruppe_Id(Long gruppeId);

    /* =========================================================
   BUSINESS CHECK (Leiter)
   ========================================================= */
    @Query("""
select count(t) > 0
from Teilnehmer t
where t.veranstaltung.id = :veranstaltungId
  and (t.rolle is null or t.rolle <> :leiter)
""")
    boolean existsNonLeiter(
            Long veranstaltungId,
            TeilnehmerRolle leiter
    );

    long countByVeranstaltungIdAndRolle(Long veranstaltungId, TeilnehmerRolle rolle);

    long countByVeranstaltungIdAndRolleIsNull(Long veranstaltungId);

    @Query("""

select count(t)
from Teilnehmer t
join t.person p
where t.veranstaltung.id = :veranstaltungId
and t.rolle is null
and p.geburtsdatum is not null
and p.geburtsdatum between :maxGeburtsdatum and :minGeburtsdatum
""")
    long countFoerderfaehigeTeilnehmer(
            @Param("veranstaltungId") Long veranstaltungId,
            @Param("minGeburtsdatum") LocalDate minGeburtsdatum,
            @Param("maxGeburtsdatum") LocalDate maxGeburtsdatum
    );
}
