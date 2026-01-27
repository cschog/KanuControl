package com.kcserver.repository;

import com.kcserver.dto.PersonListDTO;
import com.kcserver.entity.Person;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
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

    /* Liste */
    @Query("""
      select new com.kcserver.dto.PersonListDTO(
        p.id, p.vorname, p.name, p.sex, p.aktiv, p.geburtsdatum,
        size(p.mitgliedschaften)
      )
      from Person p
    """)
    Page<PersonListDTO> findAllList(Pageable pageable);

    /* Detail */
    @EntityGraph(attributePaths = {
            "mitgliedschaften",
            "mitgliedschaften.verein"
    })
    Optional<Person> findDetailById(Long id);
}