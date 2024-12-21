package com.kcserver.controller;

import com.kcserver.dto.PersonDTO;
import com.kcserver.filter.TenantContext;
import com.kcserver.service.PersonService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/person")
public class PersonController {

    private final PersonService personService;

    private static final Logger logger = LoggerFactory.getLogger(PersonController.class);

    @Autowired
    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    /**
     * Retrieve all persons as PersonDTOs, including their Mitgliedschaften and related Verein data.
     *
     * @return List of PersonDTOs.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PersonDTO>> getAllPersons() {
        List<PersonDTO> personDTOs = personService.getAllPersonsWithDetails();
        logger.info("Current Tenant: {}", TenantContext.getTenantId());
        return ResponseEntity.ok(personDTOs);
    }

    /**
     * Retrieve a person by their ID as PersonDTO, including their Mitgliedschaften and related Verein data.
     *
     * @param id The ID of the person.
     * @return The PersonDTO or 404 if not found.
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PersonDTO> getPersonById(@PathVariable Long id) {
        PersonDTO personDTO = personService.getPersonWithDetails(id);
        logger.info("Current Tenant: {}", TenantContext.getTenantId());
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
        logger.info("Current Tenant: {}", TenantContext.getTenantId());
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

        logger.info("Received PUT request to update person with ID: {} and payload: {}", id, personDTO);

        try {
            PersonDTO updatedPerson = personService.updatePerson(id, personDTO);
            logger.info("Successfully updated person with ID: {}", id);
            logger.info("Current Tenant: {}", TenantContext.getTenantId());
            return ResponseEntity.ok(updatedPerson);
        } catch (Exception e) {
            logger.error("Error updating person with ID: {}", id, e);
            throw e; // Let Spring handle and return the appropriate error response
        }
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
            logger.info("Current Tenant: {}", TenantContext.getTenantId());
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
