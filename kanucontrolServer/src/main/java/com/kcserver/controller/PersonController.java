package com.kcserver.controller;

import com.kcserver.dto.PersonDTO;
import com.kcserver.service.PersonService;
import com.kcserver.tenancy.TenantContext;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/person")
public class PersonController {

    private static final Logger logger = LoggerFactory.getLogger(PersonController.class);

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    /* =========================================================
       READ
       ========================================================= */

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PersonDTO>> getAllPersons() {
        logger.debug("GET /api/person | tenant={}", TenantContext.getTenant());
        return ResponseEntity.ok(personService.getAllPersons());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PersonDTO> getPersonById(@PathVariable long id) {
        logger.debug("GET /api/person/{} | tenant={}", id, TenantContext.getTenant());
        return ResponseEntity.ok(personService.getPerson(id));
    }

    /* =========================================================
       CREATE
       ========================================================= */

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PersonDTO> createPerson(@Valid @RequestBody PersonDTO personDTO) {
        logger.info("POST /api/person | tenant={}", TenantContext.getTenant());
        PersonDTO created = personService.createPerson(personDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PersonDTO> updatePerson(
            @PathVariable long id,
            @Valid @RequestBody PersonDTO personDTO) {

        logger.info("PUT /api/person/{} | tenant={}", id, TenantContext.getTenant());
        PersonDTO updated = personService.updatePerson(id, personDTO);
        return ResponseEntity.ok(updated);
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable long id) {
        logger.info("DELETE /api/person/{} | tenant={}", id, TenantContext.getTenant());
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }
}