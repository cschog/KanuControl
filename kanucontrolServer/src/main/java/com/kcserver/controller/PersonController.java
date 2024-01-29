package com.kcserver.controller;

import com.kcserver.dto.PersonCreateDTO;
import com.kcserver.dto.PersonWithVereinDTO;
import com.kcserver.entity.Person;
import com.kcserver.service.PersonService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PersonController {
    private final PersonService personService;

    @Autowired
    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping(value = "/person", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<Person> getPersonen() {
        return personService.getAllPersonen();
    }

    @GetMapping(value = "/person/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Person getPerson(@PathVariable long id){
        return personService.getPerson(id);
    }

    @PostMapping(value = "/person", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public Person createPerson(@Valid @RequestBody Person person) {
//        return personService.createPerson(person);
//    }

    public ResponseEntity<Person> createPerson(@RequestBody PersonCreateDTO personDTO) {
        Person createdPerson = personService.createPersonWithVerein(personDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPerson);
    }


    @DeleteMapping(value = "/person/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deletePerson(@PathVariable long id) {
        boolean deleted = personService.deletePerson(id);

        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(value = "/person/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Person> updatePerson(@PathVariable long id, @Valid @RequestBody Person updatedPerson) {
        Person updated = personService.updatePerson(id, updatedPerson);

        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/mitglied", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PersonWithVereinDTO> getPersonsWithVerein() {
        return personService.getAllPersonenWithVerein();
    }

}



