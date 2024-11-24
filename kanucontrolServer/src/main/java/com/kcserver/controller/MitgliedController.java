package com.kcserver.controller;

import com.kcserver.dto.MitgliedDTO;
import com.kcserver.dto.PersonDTO;
import com.kcserver.dto.VereinDTO;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.service.MitgliedService;
import com.kcserver.service.PersonService;
import com.kcserver.service.VereinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

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
            // Fetch Person and Verein entities from the DTOs
            Person personEntity = personService.convertToEntity(personService.getPerson(mitgliedDTO.getPersonId()));
            Verein vereinEntity = vereinService.convertToEntity(vereinService.getVerein(mitgliedDTO.getVereinId()));

            // Create a new Mitglied entity
            Mitglied mitglied = mitgliedService.createMitglied(
                    personEntity,
                    vereinEntity,
                    mitgliedDTO.getFunktion(),
                    mitgliedDTO.getHauptVerein()
            );

            // Convert the saved Mitglied to a DTO
            MitgliedDTO savedDTO = mitgliedService.toDTO(mitglied);

            logger.info("Created new Mitglied: {}", savedDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error creating Mitglied: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Unexpected error creating Mitglied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
            List<MitgliedDTO> mitgliedDTOs = mitgliedService.getMitgliedByPersonId(personId).stream()
                    .map(mitgliedService::toDTO)
                    .collect(Collectors.toList());

            logger.info("Retrieved {} Mitglied entries for Person ID {}", mitgliedDTOs.size(), personId);
            return ResponseEntity.ok(mitgliedDTOs);
        } catch (Exception e) {
            logger.error("Error retrieving Mitglied for Person ID {}: {}", personId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
            List<MitgliedDTO> mitgliedDTOs = mitgliedService.getMitgliedByVereinId(vereinId).stream()
                    .map(mitgliedService::toDTO)
                    .collect(Collectors.toList());

            logger.info("Retrieved {} Mitglied entries for Verein ID {}", mitgliedDTOs.size(), vereinId);
            return ResponseEntity.ok(mitgliedDTOs);
        } catch (Exception e) {
            logger.error("Error retrieving Mitglied for Verein ID {}: {}", vereinId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
