package com.kcserver.repository;

import com.kcserver.entity.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
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
       LIST
       ========================= */

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
}