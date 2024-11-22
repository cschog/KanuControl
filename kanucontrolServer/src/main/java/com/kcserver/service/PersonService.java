package com.kcserver.service;

import com.kcserver.entity.Person;
import com.kcserver.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    @Autowired
    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    /**
     * Retrieves all Person entities.
     *
     * @return a list of all persons
     */
    public List<Person> getAllPersonen() {
        return personRepository.findAll();
    }

    /**
     * Retrieves a Person by their unique ID.
     *
     * @param id the ID of the person
     * @return the person object
     * @throws ResponseStatusException if person with the given ID is not found
     */
    public Person getPerson(long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Person with ID %s not found", id)));
    }

    /**
     * Retrieves a Person by their exact name.
     *
     * @param name the name of the person
     * @return the person object
     * @throws ResponseStatusException if no person with the given name is found
     */
    public Person getPersonByName(String name) {
        return Optional.ofNullable(personRepository.findByNameIs(name))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Person with name %s not found", name)));
    }

    /**
     * Creates a new person.
     *
     * @param person the person object to be saved
     * @return the saved person
     */
    public Person createPerson(Person person) {
        return personRepository.save(person);
    }

    /**
     * Updates an existing person.
     *
     * @param personId      the ID of the person to be updated
     * @param updatedPerson the updated person object
     * @return the updated person, or null if not found
     * @throws ResponseStatusException if the person with the given ID doesn't exist
     */
    public Person updatePerson(long personId, Person updatedPerson) {
        Person existingPerson = getPerson(personId); // Using the getPerson method to ensure the person exists
        existingPerson.setName(updatedPerson.getName());
        existingPerson.setVorname(updatedPerson.getVorname());
        existingPerson.setStrasse(updatedPerson.getStrasse());
        existingPerson.setPlz(updatedPerson.getPlz());
        existingPerson.setOrt(updatedPerson.getOrt());
        existingPerson.setTelefon(updatedPerson.getTelefon());
        existingPerson.setBankName(updatedPerson.getBankName());
        existingPerson.setIban(updatedPerson.getIban());
        existingPerson.setBic(updatedPerson.getBic());
        return personRepository.save(existingPerson);
    }

    /**
     * Deletes a person by their ID.
     *
     * @param id the ID of the person to be deleted
     * @return true if the person was deleted successfully, otherwise false
     */
    public boolean deletePerson(long id) {
        if (personRepository.existsById(id)) {
            personRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Finds a list of people by last name.
     *
     * @param name the last name to search for
     * @return a list of people with the specified last name
     */
    public List<Person> findPeopleByName(String name) {
        return personRepository.findByName(name);
    }

    /**
     * Finds a list of people by first name starting with a given prefix.
     *
     * @param prefix the prefix to search for
     * @return a list of people whose first name starts with the given prefix
     */
    public List<Person> findPeopleByVornamePrefix(String prefix) {
        return personRepository.findByVornameStartingWith(prefix);
    }

    /**
     * Finds people by their city of residence.
     *
     * @param ort the city to search for
     * @return a list of people residing in the specified city
     */
    public List<Person> findPeopleByOrt(String ort) {
        return personRepository.findByOrt(ort);
    }

    /**
     * Finds people by their postal code.
     *
     * @param plz the postal code to search for
     * @return a list of people residing in the specified postal code
     */
    public List<Person> findPeopleByPlz(String plz) {
        return personRepository.findByPlz(plz);
    }
}