package com.kcserver.service;

import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.repository.MitgliedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MitgliedService {

    private final MitgliedRepository mitgliedRepository;

    @Autowired
    public MitgliedService(MitgliedRepository mitgliedRepository) {
        this.mitgliedRepository = mitgliedRepository;
    }

    /**
     * Creates a new Mitglied entity and saves it to the database.
     *
     * @param person      the person associated with the Mitglied
     * @param verein      the verein associated with the Mitglied
     * @param funktion    the role of the Mitglied
     * @param hauptVerein indicates whether this is the main verein for the person
     * @return the saved Mitglied entity
     */
    public Mitglied createMitglied(Person person, Verein verein, String funktion, Boolean hauptVerein) {
        if (person == null || verein == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Person and Verein cannot be null");
        }

        Mitglied mitglied = new Mitglied();
        mitglied.setPersonMitgliedschaft(person);
        mitglied.setVereinMitgliedschaft(verein);
        mitglied.setFunktion(funktion);
        mitglied.setHauptVerein(hauptVerein);

        return mitgliedRepository.save(mitglied);
    }

    /**
     * Retrieves all Mitglied entities associated with a specific Person.
     *
     * @param personId the ID of the person
     * @return a list of Mitglied entities
     */
    public List<Mitglied> getMitgliedByPersonId(Long personId) {
        return mitgliedRepository.findAll().stream()
                .filter(mitglied -> mitglied.getPersonMitgliedschaft().getId().equals(personId))
                .toList();
    }

    /**
     * Retrieves all Mitglied entities associated with a specific Verein.
     *
     * @param vereinId the ID of the verein
     * @return a list of Mitglied entities
     */
    public List<Mitglied> getMitgliedByVereinId(Long vereinId) {
        return mitgliedRepository.findAll().stream()
                .filter(mitglied -> mitglied.getVereinMitgliedschaft().getId().equals(vereinId))
                .toList();
    }

    /**
     * Deletes a Mitglied by its ID.
     *
     * @param mitgliedId the ID of the Mitglied to delete
     */
    public void deleteMitglied(Long mitgliedId) {
        if (!mitgliedRepository.existsById(mitgliedId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mitglied not found");
        }
        mitgliedRepository.deleteById(mitgliedId);
    }
}