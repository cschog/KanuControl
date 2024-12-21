package com.kcserver.service;

import com.kcserver.dto.MitgliedDTO;
import com.kcserver.dto.PersonDTO;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.mapper.EntityMapper;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VereinRepository;
import com.kcserver.sampleData.sampleService.SamplePersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PersonService {

    private static final Logger logger = LoggerFactory.getLogger(SamplePersonService.class);

    private final PersonRepository personRepository;
    private final VereinRepository vereinRepository;
    private final EntityMapper mapper;

    @Autowired
    public PersonService(PersonRepository personRepository, VereinRepository vereinRepository, EntityMapper mapper) {
        this.personRepository = personRepository;
        this.vereinRepository = vereinRepository;
        this.mapper = mapper;
    }

    /**
     * Retrieve all persons as PersonDTOs.
     *
     * @return List of PersonDTOs.
     */
    @Transactional(readOnly = true)
    public List<PersonDTO> getAllPersons() {
        return personRepository.findAll().stream()
                .map(mapper::toPersonDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a person by their ID and return as PersonDTO.
     *
     * @param id The ID of the person.
     * @return The PersonDTO.
     * @throws ResponseStatusException if the person is not found.
     */
    public PersonDTO getPerson(long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found"));
        return mapper.toPersonDTO(person);
    }

    /**
     * Retrieve a Person entity by its ID.
     *
     * @param id The ID of the person.
     * @return The Person entity.
     * @throws ResponseStatusException if the person is not found.
     */
    public Person getPersonEntityById(long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found"));
    }

    public PersonDTO getPersonWithDetails(long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found"));

        PersonDTO personDTO = mapper.toPersonDTO(person);
        personDTO.setMitgliedschaften(person.getMitgliedschaften().stream()
                .map(mapper::toMitgliedDTO)
                .collect(Collectors.toList()));
        return personDTO;
    }
    @Transactional(readOnly = true)
    public List<PersonDTO> getAllPersonsWithDetails() {
        return personRepository.findAll().stream()
                .map(person -> {
                    PersonDTO personDTO = mapper.toPersonDTO(person);
                    personDTO.setMitgliedschaften(person.getMitgliedschaften().stream()
                            .map(mapper::toMitgliedDTO)
                            .collect(Collectors.toList()));
                    return personDTO;
                })
                .collect(Collectors.toList());
    }

    /**
     * Create a new person from PersonDTO.
     *
     * @param personDTO The PersonDTO to be created.
     * @return The created PersonDTO.
     */
    public PersonDTO createPerson(PersonDTO personDTO) {
        Person person = mapper.toPersonEntity(personDTO);

        // Handle Mitgliedschaften if provided
        if (personDTO.getMitgliedschaften() != null) {
            personDTO.getMitgliedschaften().forEach(mitgliedDTO -> {
                Verein verein = vereinRepository.findById(mitgliedDTO.getVereinId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verein not found"));
                Mitglied mitglied = new Mitglied(
                        verein,
                        person,
                        mitgliedDTO.getFunktion(),
                        mitgliedDTO.getHauptVerein()
                );
                person.getMitgliedschaften().add(mitglied);
            });
        }

        Person savedPerson = personRepository.save(person);
        return mapper.toPersonDTO(savedPerson);
    }

    /**
     * Update an existing person by their ID using PersonDTO.
     *
     * @param id        The ID of the person to be updated.
     * @param personDTO The updated PersonDTO data.
     * @return The updated PersonDTO.
     * @throws ResponseStatusException if the person is not found.
     */
    public PersonDTO updatePerson(long id, PersonDTO personDTO) {
        logger.info("Updating person with ID: {} using data: {}", id, personDTO);

        Person existingPerson = personRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Person with ID: {} not found", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found");
                });

        logger.debug("Found person: {}", existingPerson);

        // Map DTO to entity
        mapper.updatePersonFromDTO(personDTO, existingPerson);
        logger.debug("Mapped fields from DTO to entity for ID: {}", id);

        // Clear existing Mitgliedschaften
        logger.info("Payload received for update: {}", personDTO);
        if (existingPerson.getMitgliedschaften() == null) {
            logger.error("Mitgliedschaften is null for person ID {}", id);
            existingPerson.setMitgliedschaften(new ArrayList<>());
        } else {
            logger.info("Existing Mitgliedschaften: {}", existingPerson.getMitgliedschaften());
            existingPerson.getMitgliedschaften().clear();
        }

        // Rebuild Mitgliedschaften from the DTO
        if (personDTO.getMitgliedschaften() != null) {
            // Map of existing Mitgliedschaften by Verein ID for quick lookup
            Map<Long, Mitglied> existingMitgliedMap = existingPerson.getMitgliedschaften().stream()
                    .collect(Collectors.toMap(m -> m.getVerein().getId(), Function.identity()));

            for (MitgliedDTO mitgliedDTO : personDTO.getMitgliedschaften()) {
                Verein verein = vereinRepository.findById(mitgliedDTO.getVereinId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verein not found"));

                Mitglied mitglied = existingMitgliedMap.get(verein.getId());
                if (mitglied == null) {
                    // New Mitglied
                    mitglied = new Mitglied();
                    mitglied.setPerson(existingPerson);
                    mitglied.setVerein(verein);
                    existingPerson.getMitgliedschaften().add(mitglied);
                }

                // Update fields
                mitglied.setFunktion(mitgliedDTO.getFunktion());
                mitglied.setHauptVerein(mitgliedDTO.getHauptVerein());
            }

            // Remove Mitgliedschaften not in the DTO
            Set<Long> incomingVereinIds = personDTO.getMitgliedschaften().stream()
                    .map(MitgliedDTO::getVereinId)
                    .collect(Collectors.toSet());

            existingPerson.getMitgliedschaften().removeIf(m -> !incomingVereinIds.contains(m.getVerein().getId()));
        }

        Person updatedPerson = personRepository.save(existingPerson);
        logger.info("Successfully updated person with ID: {}", id);
        return mapper.toPersonDTO(updatedPerson);
    }
    /**
     * Delete a person by their ID.
     *
     * @param id The ID of the person to delete.
     * @return true if successful, false otherwise.
     */
    public boolean deletePerson(long id) {
        if (personRepository.existsById(id)) {
            personRepository.deleteById(id);
            return true;
        }
        return false;
    }
}