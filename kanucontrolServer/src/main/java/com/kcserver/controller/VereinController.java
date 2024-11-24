package com.kcserver.controller;

import com.kcserver.dto.VereinDTO;
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
     * Retrieve all Vereine as VereinDTOs.
     *
     * @return List of VereinDTOs.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<VereinDTO>> getAllVereine() {
        List<VereinDTO> vereinDTOs = vereinService.getAllVereine();
        return ResponseEntity.ok(vereinDTOs);
    }

    /**
     * Retrieve a Verein by its ID as VereinDTO.
     *
     * @param id The ID of the Verein.
     * @return The VereinDTO or 404 if not found.
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VereinDTO> getVereinById(@PathVariable Long id) {
        VereinDTO vereinDTO = vereinService.getVerein(id);
        return ResponseEntity.ok(vereinDTO);
    }

    /**
     * Create a new Verein from a VereinDTO.
     *
     * @param vereinDTO The VereinDTO to be created.
     * @return The created VereinDTO.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VereinDTO> createVerein(@Valid @RequestBody VereinDTO vereinDTO) {
        VereinDTO createdVereinDTO = vereinService.createVerein(vereinDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVereinDTO);
    }

    /**
     * Update an existing Verein by its ID using VereinDTO.
     *
     * @param id            The ID of the Verein to be updated.
     * @param updatedVereinDTO The updated VereinDTO data.
     * @return The updated VereinDTO or 404 if not found.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VereinDTO> updateVerein(@PathVariable Long id, @Valid @RequestBody VereinDTO updatedVereinDTO) {
        VereinDTO updatedVereinDTOResult = vereinService.updateVerein(id, updatedVereinDTO);
        return ResponseEntity.ok(updatedVereinDTOResult);
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
