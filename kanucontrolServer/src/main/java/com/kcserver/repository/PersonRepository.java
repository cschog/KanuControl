package com.kcserver.repository;

import com.kcserver.entity.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

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
       LIST
       ========================= */

    @Query("""
SELECT p FROM Person p
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
ORDER BY p.name ASC, p.vorname ASC, p.id ASC
""")
    List<Person> scroll(
            String cursorName,
            String cursorVorname,
            Long cursorId,
            String search
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
}