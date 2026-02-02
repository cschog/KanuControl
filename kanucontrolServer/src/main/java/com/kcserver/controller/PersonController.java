package com.kcserver.controller;

import com.kcserver.dto.PersonDetailDTO;
import com.kcserver.dto.PersonListDTO;
import com.kcserver.dto.PersonSaveDTO;
import com.kcserver.dto.PersonSearchCriteria;
import com.kcserver.service.PersonService;
import com.kcserver.validation.OnCreate;
import com.kcserver.validation.OnUpdate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/person")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    /* =========================
       LIST (PAGINIERT)
       ========================= */

    @GetMapping
    public Page<PersonListDTO> getAll(Pageable pageable) {
        return personService.getAll(pageable);
    }

    /* =========================
       SEARCH (PAGINIERT)
       ========================= */

    @GetMapping("/search")
    public Page<PersonListDTO> search(
            PersonSearchCriteria criteria,
            Pageable pageable
    ) {
        return personService.searchList(criteria, pageable);
    }

    /* =========================
       DETAIL
       ========================= */

    @GetMapping("/{id}")
    public PersonDetailDTO getPerson(@PathVariable long id) {
        return personService.getPersonDetail(id);
    }

    /* =========================
       CREATE
       ========================= */

    @PostMapping
    public ResponseEntity<PersonDetailDTO> createPerson(
            @Validated(OnCreate.class) @RequestBody PersonSaveDTO dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(personService.createPerson(dto));
    }

    /* =========================
       UPDATE
       ========================= */

    @PutMapping("/{id}")
    public PersonDetailDTO updatePerson(
            @PathVariable long id,
            @Validated(OnUpdate.class) @RequestBody PersonSaveDTO dto
    ) {
        return personService.updatePerson(id, dto);
    }

    /* =========================
       DELETE
       ========================= */

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePerson(@PathVariable long id) {
        personService.deletePerson(id);
    }
}