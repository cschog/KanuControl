package com.kcserver.repository;

import com.kcserver.entity.Verein;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VereinRepository extends JpaRepository<Verein, Long> {

    /**
     * Finds a list of Verein entities by their exact name.
     *
     * @param name the name of the Verein
     * @return a list of Verein entities
     */
    List<Verein> findByName(String name);

    /**
     * Finds a single Verein entity by its exact name.
     *
     * @param name the name of the Verein
     * @return a Verein entity
     */
    Verein findByNameIs(String name);

    /**
     * Finds a list of Verein entities whose name starts with a specific prefix.
     *
     * @param prefix the prefix to search for
     * @return a list of Verein entities
     */
    List<Verein> findByNameStartingWith(String prefix);

    /**
     * Finds all Verein entities located in a specific city (ort).
     *
     * @param ort the city to search for
     * @return a list of Verein entities
     */
    List<Verein> findByOrt(String ort);

    /**
     * Finds all Verein entities located in a specific postal code (plz).
     *
     * @param plz the postal code to search for
     * @return a list of Verein entities
     */
    List<Verein> findByPlz(String plz);

    /**
     * Finds all Verein entities by a specific abbreviation (abk).
     *
     * @param abk the abbreviation to search for
     * @return a list of Verein entities
     */
    List<Verein> findByAbk(String abk);
}