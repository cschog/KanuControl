package com.kcserver.repository;

import com.kcserver.entity.Mitglied;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MitgliedRepository extends JpaRepository<Mitglied, Long> {

        /**
         * Finds all Mitglied entities associated with a specific person by their ID.
         *
         * @param personId the ID of the person
         * @return a list of Mitglied entities
         */
        List<Mitglied> findByPersonMitgliedschaft_Id(Long personId);

        /**
         * Finds all Mitglied entities associated with a specific verein by their ID.
         *
         * @param vereinId the ID of the verein
         * @return a list of Mitglied entities
         */
        List<Mitglied> findByVereinMitgliedschaft_Id(Long vereinId);

        /**
         * Finds all Mitglied entities where `hauptVerein` is true.
         *
         * @param hauptVerein true or false indicating whether the Mitglied belongs to the main verein
         * @return a list of Mitglied entities
         */
        List<Mitglied> findByHauptVerein(boolean hauptVerein);

        @EntityGraph(attributePaths = {"personMitgliedschaft", "vereinMitgliedschaft"})
        List<Mitglied> findAll();
}