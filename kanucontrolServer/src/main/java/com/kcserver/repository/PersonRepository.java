package com.kcserver.repository;

import com.kcserver.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    /**
     * Finds a list of Person entities by their last name.
     *
     * @param name the last name to search for
     * @return a list of Person entities
     */
    List<Person> findByName(String name);

    /**
     * Finds a single Person entity by their exact last name.
     *
     * @param name the exact last name to search for
     * @return a Person entity
     */
    Person findByNameIs(String name);

    /**
     * Finds a single Person entity by both first and last name.
     *
     * @param vorname the first name
     * @param name    the last name
     * @return a Person entity
     */
    Person findByVornameAndName(String vorname, String name);

    /**
     * Finds a list of Person entities whose last name starts with a specific prefix.
     *
     * @param prefix the prefix to search for
     * @return a list of Person entities
     */
    List<Person> findByNameStartingWith(String prefix);

    /**
     * Finds a list of Person entities whose first name starts with a specific prefix.
     *
     * @param prefix the prefix to search for
     * @return a list of Person entities
     */
    List<Person> findByVornameStartingWith(String prefix);

    /**
     * Finds all Person entities living in a specific city (ort).
     *
     * @param ort the city to search for
     * @return a list of Person entities
     */
    List<Person> findByOrt(String ort);

    /**
     * Finds all Person entities living in a specific postal code (plz).
     *
     * @param plz the postal code to search for
     * @return a list of Person entities
     */
    List<Person> findByPlz(String plz);
}