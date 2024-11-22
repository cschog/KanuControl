package com.kcserver.controller;

import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.service.MitgliedService;
import com.kcserver.service.PersonService;
import com.kcserver.service.VereinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mitglied")
public class MitgliedController {
    private final MitgliedService mitgliedService;
    private final PersonService personService;
    private final VereinService vereinService;

    @Autowired
    public MitgliedController(MitgliedService mitgliedService, PersonService personService, VereinService vereinService) {
        this.mitgliedService = mitgliedService;
        this.personService = personService;
        this.vereinService = vereinService;
    }

    @PostMapping
    public ResponseEntity<Mitglied> createMitglied(
            @RequestParam Long personId,
            @RequestParam Long vereinId,
            @RequestParam String funktion,
            @RequestParam Boolean hauptVerein) {
        Person person = personService.getPerson(personId);
        Verein verein = vereinService.getVerein(vereinId);
        Mitglied mitglied = mitgliedService.createMitglied(person, verein, funktion, hauptVerein);
        return ResponseEntity.ok(mitglied);
    }

    @GetMapping("/person/{personId}")
    public List<Mitglied> getMitgliedByPerson(@PathVariable Long personId) {
        return mitgliedService.getMitgliedByPersonId(personId);
    }

    @GetMapping("/verein/{vereinId}")
    public List<Mitglied> getMitgliedByVerein(@PathVariable Long vereinId) {
        return mitgliedService.getMitgliedByVereinId(vereinId);
    }

    @DeleteMapping("/{mitgliedId}")
    public ResponseEntity<Void> deleteMitglied(@PathVariable Long mitgliedId) {
        mitgliedService.deleteMitglied(mitgliedId);
        return ResponseEntity.noContent().build();
    }
}