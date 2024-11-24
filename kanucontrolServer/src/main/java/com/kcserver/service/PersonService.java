package com.kcserver.service;

import com.kcserver.dto.PersonDTO;
import com.kcserver.entity.Person;
import com.kcserver.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    @Autowired
    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    /**
     * Retrieve all persons as PersonDTOs.
     *
     * @return List of PersonDTOs.
     */
    public List<PersonDTO> getAllPersons() {
        return personRepository.findAll().stream()
                .map(this::convertToDTO)
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
        return convertToDTO(person);
    }

    /**
     * Create a new person from PersonDTO.
     *
     * @param personDTO The PersonDTO to be created.
     * @return The created PersonDTO.
     */
    public PersonDTO createPerson(PersonDTO personDTO) {
        Person person = convertToEntity(personDTO);
        Person savedPerson = personRepository.save(person);
        return convertToDTO(savedPerson);
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

        existingPerson.setName(personDTO.getName());
        existingPerson.setVorname(personDTO.getVorname());
        existingPerson.setStrasse(personDTO.getStrasse());
        existingPerson.setPlz(personDTO.getPlz());
        existingPerson.setOrt(personDTO.getOrt());
        existingPerson.setTelefon(personDTO.getTelefon());
        existingPerson.setBankName(personDTO.getBankName());
        existingPerson.setIban(personDTO.getIban());
        existingPerson.setBic(personDTO.getBic());

        Person updatedPerson = personRepository.save(existingPerson);
        return convertToDTO(updatedPerson);
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

    /**
     * Converts a Person entity to a PersonDTO.
     *
     * @param person The Person entity.
     * @return The corresponding PersonDTO.
     */
    private PersonDTO convertToDTO(Person person) {
        return new PersonDTO(
                person.getId(),
                person.getName(),
                person.getVorname(),
                person.getStrasse(),
                person.getPlz(),
                person.getOrt(),
                person.getTelefon(),
                person.getBankName(),
                person.getIban(),
                person.getBic()
        );
    }

    /**
     * Converts a PersonDTO to a Person entity.
     *
     * @param personDTO The PersonDTO.
     * @return The corresponding Person entity.
     */
    public Person convertToEntity(PersonDTO personDTO) {
        return new Person(
                personDTO.getName(),
                personDTO.getVorname(),
                personDTO.getStrasse(),
                personDTO.getPlz(),
                personDTO.getOrt(),
                personDTO.getTelefon(),
                personDTO.getBankName(),
                personDTO.getIban(),
                personDTO.getBic()
        );
    }
}
