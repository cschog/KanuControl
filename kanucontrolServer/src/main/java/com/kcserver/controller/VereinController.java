package com.kcserver.controller;

import com.kcserver.entity.Verein;
import com.kcserver.service.VereinService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class VereinController {
    private final VereinService vereinService;

    @Autowired
    public VereinController(VereinService vereinService) {
        this.vereinService = vereinService;
    }

    @GetMapping(value = "/verein", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<Verein> getVereine() {
        return vereinService.getAllVereine();
    }

    @GetMapping(value = "/verein/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Verein getVerein(@PathVariable long id){
        return vereinService.getVerein(id);
    }

    @PostMapping(value = "/verein", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Verein createVerein(@Valid @RequestBody Verein verein) {
        return vereinService.createVerein(verein);
    }

    @DeleteMapping(value = "/verein/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteVerein(@PathVariable long id) {
        boolean deleted = vereinService.deleteVerein(id);

        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(value = "/verein/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Verein> updateVerein(@PathVariable long id, @Valid @RequestBody Verein updatedVerein) {
        Verein updated = vereinService.updateVerein(id, updatedVerein);

        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}



