package com.kcserver.service;

import com.kcserver.dto.PersonDTO;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VereinRepository;
import com.kcserver.mapper.EntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonService {

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
        Person existingPerson = personRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found"));

        mapper.updatePersonFromDTO(personDTO, existingPerson);

        // Update Mitgliedschaften
        existingPerson.getMitgliedschaften().clear();
        if (personDTO.getMitgliedschaften() != null) {
            personDTO.getMitgliedschaften().forEach(mitgliedDTO -> {
                Verein verein = vereinRepository.findById(mitgliedDTO.getVereinId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verein not found"));
                Mitglied mitglied = new Mitglied(
                        verein,
                        existingPerson,
                        mitgliedDTO.getFunktion(),
                        mitgliedDTO.getHauptVerein()
                );
                existingPerson.getMitgliedschaften().add(mitglied);
            });
        }

        Person updatedPerson = personRepository.save(existingPerson);
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