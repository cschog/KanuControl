package com.kcserver.service;

import com.kcserver.dto.MitgliedDTO;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.repository.MitgliedRepository;
import com.kcserver.mapper.EntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MitgliedService {

    private final MitgliedRepository mitgliedRepository;
    private final EntityMapper mapper;

    @Autowired
    public MitgliedService(MitgliedRepository mitgliedRepository, EntityMapper mapper) {
        this.mitgliedRepository = mitgliedRepository;
        this.mapper = mapper;
    }

    /**
     * Creates a new Mitglied entity, saves it to the database, and returns the corresponding DTO.
     *
     * @param person      the person associated with the Mitglied
     * @param verein      the verein associated with the Mitglied
     * @param funktion    the role of the Mitglied
     * @param hauptVerein indicates whether this is the main verein for the person
     * @return the saved Mitglied as a DTO
     */
    public MitgliedDTO createMitglied(Person person, Verein verein, String funktion, Boolean hauptVerein) {
        if (person == null || verein == null) {
            throw new IllegalArgumentException("Person and Verein cannot be null");
        }

        Mitglied mitglied = new Mitglied();
        mitglied.setPersonMitgliedschaft(person);
        mitglied.setVereinMitgliedschaft(verein);
        mitglied.setFunktion(funktion);
        mitglied.setHauptVerein(hauptVerein);

        Mitglied savedMitglied = mitgliedRepository.save(mitglied);
        return mapper.toMitgliedDTO(savedMitglied);
    }

    /**
     * Updates an existing Mitglied entity.
     *
     * @param mitgliedId the ID of the Mitglied to update
     * @param funktion   the updated role of the Mitglied
     * @param hauptVerein indicates whether this is the main verein
     * @return the updated Mitglied as a DTO
     */
    public MitgliedDTO updateMitglied(Long mitgliedId, String funktion, Boolean hauptVerein) {
        Mitglied mitglied = mitgliedRepository.findById(mitgliedId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mitglied not found"));

        mitglied.setFunktion(funktion);
        mitglied.setHauptVerein(hauptVerein);

        Mitglied updatedMitglied = mitgliedRepository.save(mitglied);
        return mapper.toMitgliedDTO(updatedMitglied);
    }

    /**
     * Retrieves all Mitglied entities associated with a specific Person and converts them to DTOs.
     *
     * @param personId the ID of the person
     * @return a list of MitgliedDTOs
     */
    public List<MitgliedDTO> getMitgliedByPersonId(Long personId) {
        return mitgliedRepository.findByPersonMitgliedschaft_Id(personId).stream()
                .map(mapper::toMitgliedDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all Mitglied entities associated with a specific Verein and converts them to DTOs.
     *
     * @param vereinId the ID of the verein
     * @return a list of MitgliedDTOs
     */
    public List<MitgliedDTO> getMitgliedByVereinId(Long vereinId) {
        return mitgliedRepository.findByVereinMitgliedschaft_Id(vereinId).stream()
                .map(mapper::toMitgliedDTO)
                .collect(Collectors.toList());
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

    /**
     * Retrieves all Mitglieds and converts them to DTOs.
     *
     * @return a list of all MitgliedDTOs
     */
    public List<MitgliedDTO> getAllMitglieds() {
        return mitgliedRepository.findAll().stream()
                .map(mapper::toMitgliedDTO)
                .collect(Collectors.toList());
    }
}