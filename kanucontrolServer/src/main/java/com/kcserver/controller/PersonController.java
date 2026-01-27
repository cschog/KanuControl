package com.kcserver.controller;

import com.kcserver.dto.PersonDetailDTO;
import com.kcserver.dto.PersonListDTO;
import com.kcserver.dto.PersonSaveDTO;
import com.kcserver.dto.PersonSearchCriteria;
import com.kcserver.service.PersonService;
import com.kcserver.validation.OnCreate;
import com.kcserver.validation.OnUpdate;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/person")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    /* =========================================================
       LIST / SEARCH
       ========================================================= */

    @GetMapping
    public ResponseEntity<List<PersonListDTO>> getAllPersons() {
        return ResponseEntity.ok(personService.getAllPersonsList());
    }

    @GetMapping("/search")
    public ResponseEntity<List<PersonListDTO>> searchPersons(
            PersonSearchCriteria criteria,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                personService.searchList(criteria, pageable).getContent()
        );
    }

    /* =========================================================
       DETAIL
       ========================================================= */

    @GetMapping("/{id}")
    public ResponseEntity<PersonDetailDTO> getPersonById(@PathVariable long id) {
        return ResponseEntity.ok(personService.getPersonDetail(id));
    }

    /* =========================================================
       CREATE / UPDATE
       ========================================================= */

    @PostMapping
    public ResponseEntity<PersonDetailDTO> createPerson(
            @Validated(OnCreate.class) @RequestBody PersonSaveDTO dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(personService.createPerson(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonDetailDTO> updatePerson(
            @PathVariable long id,
            @Validated(OnUpdate.class) @RequestBody PersonSaveDTO dto
    ) {
        return ResponseEntity.ok(personService.updatePerson(id, dto));
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable long id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }
}