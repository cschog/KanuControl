package com.kcserver.repository;

import com.kcserver.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Long> {
    List<Person> findByName(String name);
    Person findByNameIs(String name);

    Person findByVornameAndName(String vorname, String name);
    List<Person> findByNameStartingWith(String prefix);
}
