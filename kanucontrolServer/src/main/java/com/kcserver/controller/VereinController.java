package com.kcserver.controller;

import com.kcserver.entity.Verein;
import com.kcserver.service.VereinService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vereine")
public class VereinController {

    private final VereinService vereinService;

    @Autowired
    public VereinController(VereinService vereinService) {
        this.vereinService = vereinService;
    }

    /**
     * Retrieve all Vereine.
     *
     * @return List of Vereine.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Verein>> getAllVereine() {
        List<Verein> vereine = vereinService.getAllVereine();
        return ResponseEntity.ok(vereine);
    }

    /**
     * Retrieve a Verein by its ID.
     *
     * @param id The ID of the Verein.
     * @return The Verein or 404 if not found.
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Verein> getVereinById(@PathVariable Long id) {
        Verein verein = vereinService.getVerein(id);
        return ResponseEntity.ok(verein);
    }

    /**
     * Create a new Verein.
     *
     * @param verein The Verein to be created.
     * @return The created Verein.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Verein> createVerein(@Valid @RequestBody Verein verein) {
        Verein createdVerein = vereinService.createVerein(verein);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVerein);
    }

    /**
     * Update an existing Verein by its ID.
     *
     * @param id            The ID of the Verein to be updated.
     * @param updatedVerein The updated Verein data.
     * @return The updated Verein or 404 if not found.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Verein> updateVerein(@PathVariable Long id, @Valid @RequestBody Verein updatedVerein) {
        Verein updated = vereinService.updateVerein(id, updatedVerein);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * Delete a Verein by its ID.
     *
     * @param id The ID of the Verein to delete.
     * @return No content if successful, or 404 if not found.
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteVerein(@PathVariable Long id) {
        boolean isDeleted = vereinService.deleteVerein(id);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}