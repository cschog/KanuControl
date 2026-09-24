package com.kcserver.repository;

import com.kcserver.entity.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PersonRepository
        extends JpaRepository<Person, Long>,
        JpaSpecificationExecutor<Person> {

    boolean existsByVornameAndNameAndGeburtsdatum(
            String vorname,
            String name,
            LocalDate geburtsdatum
    );

    Optional<Person> findByVornameAndNameAndGeburtsdatum(
            String vorname,
            String name,
            LocalDate geburtsdatum
    );

    /* =========================
       LIST / SCROLL
       ========================= */

    @Query("""
SELECT DISTINCT p
FROM Person p
LEFT JOIN p.mitgliedschaften m
LEFT JOIN m.verein v
WHERE (
    :cursorName IS NULL OR
    (p.name > :cursorName) OR
    (p.name = :cursorName AND p.vorname > :cursorVorname) OR
    (p.name = :cursorName AND p.vorname = :cursorVorname AND p.id > :cursorId)
)
AND (
    :search IS NULL OR :search = '' OR
    LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
    LOWER(p.vorname) LIKE LOWER(CONCAT('%', :search, '%'))
)
AND (
    :ort IS NULL OR :ort = '' OR
    LOWER(p.ort) LIKE LOWER(CONCAT('%', :ort, '%'))
)
AND (
    :vereinId IS NULL
    OR v.id = :vereinId
)
AND (
    :aktiv IS NULL OR
    p.aktiv = :aktiv
)
ORDER BY p.name ASC, p.vorname ASC, p.id ASC
""")
    Slice<Person> scroll(
            @Param("cursorName") String cursorName,
            @Param("cursorVorname") String cursorVorname,
            @Param("cursorId") Long cursorId,
            @Param("search") String search,
            @Param("ort") String ort,
            @Param("vereinId") Long vereinId,
            @Param("aktiv") Boolean aktiv,
            Pageable pageable
    );

    @Query("""
SELECT DISTINCT p
FROM Person p
LEFT JOIN p.mitgliedschaften m
LEFT JOIN m.verein v
WHERE (
    :cursorName IS NULL OR
    (p.name < :cursorName) OR
    (p.name = :cursorName AND p.vorname < :cursorVorname) OR
    (p.name = :cursorName AND p.vorname = :cursorVorname AND p.id < :cursorId)
)
AND (
    :search IS NULL OR :search = '' OR
    LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
    LOWER(p.vorname) LIKE LOWER(CONCAT('%', :search, '%'))
)
AND (
    :ort IS NULL OR :ort = '' OR
    LOWER(p.ort) LIKE LOWER(CONCAT('%', :ort, '%'))
)
AND (
    :vereinId IS NULL
    OR v.id = :vereinId
)
AND (
    :aktiv IS NULL OR
    p.aktiv = :aktiv
)
ORDER BY p.name DESC, p.vorname DESC, p.id DESC
""")
    Slice<Person> scrollDesc(
            @Param("cursorName") String cursorName,
            @Param("cursorVorname") String cursorVorname,
            @Param("cursorId") Long cursorId,
            @Param("search") String search,
            @Param("ort") String ort,
            @Param("vereinId") Long vereinId,
            @Param("aktiv") Boolean aktiv,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "mitgliedschaften",
            "mitgliedschaften.verein"
    })
    Page<Person> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {
            "mitgliedschaften",
            "mitgliedschaften.verein"
    })
    Page<Person> findAll(
            org.springframework.data.jpa.domain.Specification<Person> spec,
            Pageable pageable
    );

    /* =========================
       DETAIL
       ========================= */

    @EntityGraph(attributePaths = {
            "mitgliedschaften",
            "mitgliedschaften.verein"
    })
    Optional<Person> findDetailById(Long id);

    /* =========================
       SEARCH REF
       ========================= */

    @Query("""
SELECT DISTINCT p
FROM Person p
LEFT JOIN FETCH p.mitgliedschaften m
LEFT JOIN FETCH m.verein
WHERE (
    :search IS NULL
    OR :search = ''
    OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
    OR LOWER(p.vorname) LIKE LOWER(CONCAT('%', :search, '%'))
)
ORDER BY p.name, p.vorname
""")
    List<Person> searchRefList(@Param("search") String search);

    @Query("""
SELECT DISTINCT p
FROM Person p
LEFT JOIN FETCH p.mitgliedschaften m
LEFT JOIN FETCH m.verein
WHERE p.geburtsdatum IS NOT NULL
AND p.geburtsdatum <= :stichtag
AND (
    :search IS NULL
    OR :search = ''
    OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
    OR LOWER(p.vorname) LIKE LOWER(CONCAT('%', :search, '%'))
)
ORDER BY p.name, p.vorname
""")
    List<Person> searchLeiterRefList(
            @Param("search") String search,
            @Param("stichtag") LocalDate stichtag
    );
}