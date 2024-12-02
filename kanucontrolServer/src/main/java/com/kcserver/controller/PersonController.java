package com.kcserver.controller;

import com.kcserver.dto.PersonDTO;
import com.kcserver.service.PersonService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/person")
public class PersonController {

    private final PersonService personService;

    @Autowired
    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    /**
     * Retrieve all persons as PersonDTOs.
     *
     * @return List of PersonDTOs.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PersonDTO>> getAllPersons() {
        List<PersonDTO> personDTOs = personService.getAllPersons();
        return ResponseEntity.ok(personDTOs);
    }

    /**
     * Retrieve a person by their ID as PersonDTO.
     *
     * @param id The ID of the person.
     * @return The PersonDTO or 404 if not found.
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PersonDTO> getPersonById(@PathVariable Long id) {
        PersonDTO personDTO = personService.getPerson(id);
        return ResponseEntity.ok(personDTO);
    }

    /**
     * Create a new person from a PersonDTO.
     *
     * @param personDTO The PersonDTO to be created.
     * @return The created PersonDTO.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PersonDTO> createPerson(@Valid @RequestBody PersonDTO personDTO) {
        PersonDTO createdPersonDTO = personService.createPerson(personDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPersonDTO);
    }

    /**
     * Update an existing person by their ID using PersonDTO.
     *
     * @param id         The ID of the person to be updated.
     * @param personDTO The updated PersonDTO data.
     * @return The updated PersonDTO or 404 if not found.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PersonDTO> updatePerson(@PathVariable Long id, @Valid @RequestBody PersonDTO personDTO) {
        PersonDTO updatedPersonDTO = personService.updatePerson(id, personDTO);
        return ResponseEntity.ok(updatedPersonDTO);
    }

    /**
     * Delete a person by their ID.
     *
     * @param id The ID of the person to delete.
     * @return No content if successful, or 404 if not found.
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        boolean isDeleted = personService.deletePerson(id);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
