package com.kcserver.service;

import com.kcserver.dto.MitgliedDTO;
import org.springframework.transaction.annotation.Transactional;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.mapper.MitgliedMapper;
import com.kcserver.repository.MitgliedRepository;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VereinRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MitgliedService {

    private final MitgliedRepository mitgliedRepository;
    private final PersonRepository personRepository;
    private final VereinRepository vereinRepository;
    private final MitgliedMapper mitgliedMapper;

    public MitgliedService(
            MitgliedRepository mitgliedRepository,
            PersonRepository personRepository,
            VereinRepository vereinRepository,
            MitgliedMapper mitgliedMapper
    ) {
        this.mitgliedRepository = mitgliedRepository;
        this.personRepository = personRepository;
        this.vereinRepository = vereinRepository;
        this.mitgliedMapper = mitgliedMapper;
    }

    /* =========================================================
       CREATE
       ========================================================= */

    @Transactional
    public MitgliedDTO createMitglied(MitgliedDTO dto) {

        Person person = personRepository.findById(dto.getPersonId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Person not found"
                ));

        Verein verein = vereinRepository.findById(dto.getVereinId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Verein not found"
                ));

        // 🔒 Fachliche Regel: nur ein Hauptverein pro Person
        if (Boolean.TRUE.equals(dto.getHauptVerein())) {
            mitgliedRepository
                    .findByPerson_IdAndHauptVereinTrue(dto.getPersonId())
                    .ifPresent(existing -> {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Person already has a Hauptverein"
                        );
                    });
        }
        if (Boolean.TRUE.equals(dto.getHauptVerein())
                && mitgliedRepository.existsByPerson_IdAndHauptVereinTrue(dto.getPersonId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Person already has a Hauptverein"
            );
        }

        Mitglied mitglied = new Mitglied();
        mitglied.setPerson(person);
        mitglied.setVerein(verein);
        mitglied.setFunktion(dto.getFunktion());      // Enum
        mitglied.setHauptVerein(dto.getHauptVerein());

        Mitglied saved = mitgliedRepository.save(mitglied);

        return mitgliedMapper.toDTO(saved);
    }

    /* =========================================================
       READ
       ========================================================= */

    @Transactional(readOnly = true)
    public MitgliedDTO getById(Long id) {
        Mitglied mitglied = mitgliedRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Mitglied not found"
                ));
        return mitgliedMapper.toDTO(mitglied);
    }

    @Transactional(readOnly = true)
    public List<MitgliedDTO> getByPerson(Long personId) {
        return mitgliedRepository.findByPerson_Id(personId)
                .stream()
                .map(mitgliedMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MitgliedDTO> getByVerein(Long vereinId) {
        return mitgliedRepository.findByVerein_Id(vereinId)
                .stream()
                .map(mitgliedMapper::toDTO)
                .toList();
    }

    /* =========================================================
   READ – HAUPTVEREIN
   ========================================================= */

    @Transactional(readOnly = true)
    public MitgliedDTO getHauptvereinByPerson(Long personId) {

        Mitglied mitglied = mitgliedRepository
                .findByPerson_IdAndHauptVereinTrue(personId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No Hauptverein found for person"
                ));

        return mitgliedMapper.toDTO(mitglied);
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @Transactional
    public void delete(Long id) {
        if (!mitgliedRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Mitglied not found"
            );
        }
        mitgliedRepository.deleteById(id);
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    @Transactional
    public MitgliedDTO updateMitglied(Long id, MitgliedDTO dto) {

        Mitglied mitglied = mitgliedRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Mitglied not found"
                ));

        // ❌ Person darf nicht geändert werden
        if (dto.getPersonId() != null &&
                !mitglied.getPerson().getId().equals(dto.getPersonId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Changing person of Mitglied is not allowed"
            );
        }

        // ❌ Verein darf nicht geändert werden
        if (dto.getVereinId() != null &&
                !mitglied.getVerein().getId().equals(dto.getVereinId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Changing verein of Mitglied is not allowed"
            );
        }

    /* =========================
       ✅ Funktion ändern
       ========================= */
        if (dto.getFunktion() != null) {
            mitglied.setFunktion(dto.getFunktion());
        }

    /* =========================
       ✅ Hauptverein wechseln
       ========================= */
        if (dto.getHauptVerein() != null) {

            if (dto.getHauptVerein() && !mitglied.getHauptVerein()) {
                // alten Hauptverein zurücksetzen
                mitgliedRepository
                        .findByPerson_IdAndHauptVereinTrue(mitglied.getPerson().getId())
                        .ifPresent(existing -> existing.setHauptVerein(false));

                mitglied.setHauptVerein(true);
            }

            if (!dto.getHauptVerein()) {
                mitglied.setHauptVerein(false);
            }
        }

        return mitgliedMapper.toDTO(mitglied);
    }
}