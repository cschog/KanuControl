package com.kcserver.controller;

import com.kcserver.dto.MitgliedDTO;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.service.MitgliedService;
import com.kcserver.service.PersonService;
import com.kcserver.service.VereinService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mitglied")
public class MitgliedController {

    private static final Logger logger = LoggerFactory.getLogger(MitgliedController.class);

    private final MitgliedService mitgliedService;
    private final PersonService personService;
    private final VereinService vereinService;

    @Autowired
    public MitgliedController(MitgliedService mitgliedService, PersonService personService, VereinService vereinService) {
        this.mitgliedService = mitgliedService;
        this.personService = personService;
        this.vereinService = vereinService;
    }

    /**
     * Creates a new Mitglied entity.
     *
     * @param mitgliedDTO the DTO containing Mitglied details
     * @return ResponseEntity with the created MitgliedDTO
     */
    @PostMapping
    public ResponseEntity<MitgliedDTO> createMitglied(@RequestBody @Valid MitgliedDTO mitgliedDTO) {
        try {
            // Validate that Person ID and Verein ID are not null
            Long personId = mitgliedDTO.getPersonId();
            Long vereinId = mitgliedDTO.getVereinId();

            if (personId == null || vereinId == null) {
                throw new IllegalArgumentException("Person ID and Verein ID must not be null");
            }

            // Fetch Person and Verein entities
            Person person = personService.getPersonEntityById(personId);
            Verein verein = vereinService.getVereinEntityById(vereinId);

            // Create the Mitglied and return the DTO
            MitgliedDTO createdMitgliedDTO = mitgliedService.createMitglied(
                    person,
                    verein,
                    mitgliedDTO.getFunktion(),
                    mitgliedDTO.getHauptVerein()
            );

            logger.info("Created new Mitglied: {}", createdMitgliedDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMitgliedDTO);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error creating Mitglied: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Unexpected error creating Mitglied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves all Mitglied entries for a given Person ID.
     *
     * @param personId the ID of the person
     * @return ResponseEntity containing a list of MitgliedDTOs
     */
    @GetMapping("/person/{personId}")
    public ResponseEntity<List<MitgliedDTO>> getMitgliedByPerson(@PathVariable @NotNull Long personId) {
        try {
            List<MitgliedDTO> mitgliedDTOs = mitgliedService.getMitgliedByPersonId(personId);

            logger.info("Retrieved {} Mitglied entries for Person ID {}", mitgliedDTOs.size(), personId);
            return ResponseEntity.ok(mitgliedDTOs);
        } catch (Exception e) {
            logger.error("Error retrieving Mitglied for Person ID {}: {}", personId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves all Mitglied entries for a given Verein ID.
     *
     * @param vereinId the ID of the Verein
     * @return ResponseEntity containing a list of MitgliedDTOs
     */
    @GetMapping("/verein/{vereinId}")
    public ResponseEntity<List<MitgliedDTO>> getMitgliedByVerein(@PathVariable @NotNull Long vereinId) {
        try {
            List<MitgliedDTO> mitgliedDTOs = mitgliedService.getMitgliedByVereinId(vereinId);

            logger.info("Retrieved {} Mitglied entries for Verein ID {}", mitgliedDTOs.size(), vereinId);
            return ResponseEntity.ok(mitgliedDTOs);
        } catch (Exception e) {
            logger.error("Error retrieving Mitglied for Verein ID {}: {}", vereinId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Deletes a Mitglied by its ID.
     *
     * @param mitgliedId the ID of the Mitglied to delete
     * @return ResponseEntity with no content if successful
     */
    @DeleteMapping("/{mitgliedId}")
    public ResponseEntity<Void> deleteMitglied(@PathVariable @NotNull Long mitgliedId) {
        try {
            mitgliedService.deleteMitglied(mitgliedId);
            logger.info("Deleted Mitglied with ID: {}", mitgliedId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.error("Validation error deleting Mitglied with ID {}: {}", mitgliedId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error deleting Mitglied with ID {}: {}", mitgliedId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}