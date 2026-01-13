package com.kcserver.repository;

import com.kcserver.entity.Person;
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
}