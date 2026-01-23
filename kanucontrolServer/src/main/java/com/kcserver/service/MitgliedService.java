package com.kcserver.service;

import com.kcserver.dto.MitgliedDTO;
import org.springframework.data.domain.Pageable;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.exception.BusinessRuleViolationException;
import com.kcserver.mapper.MitgliedMapper;
import com.kcserver.repository.MitgliedRepository;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VereinRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

        if (mitgliedRepository.existsByPerson_IdAndVerein_Id(
                dto.getPersonId(), dto.getVereinId())) {
            throw new BusinessRuleViolationException(
                    "Person is already Mitglied in this Verein"
            );
        }

        Mitglied mitglied = new Mitglied();
        mitglied.setPerson(person);
        mitglied.setVerein(verein);
        mitglied.setFunktion(dto.getFunktion());

        // ⭐ CREATE schaltet IMMER Hauptverein um
        mitgliedRepository.unsetHauptvereinByPerson(person.getId());
        mitglied.setHauptVerein(true);

        return mitgliedMapper.toDTO(
                mitgliedRepository.save(mitglied)
        );
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
    public List<MitgliedDTO> getByPerson(Long personId, Pageable pageable) {
        return mitgliedRepository
                .findByPerson_Id(personId, pageable)
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

        if (dto.getPersonId() != null &&
                !mitglied.getPerson().getId().equals(dto.getPersonId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Changing person of Mitglied is not allowed"
            );
        }

        if (dto.getVereinId() != null &&
                !mitglied.getVerein().getId().equals(dto.getVereinId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Changing verein of Mitglied is not allowed"
            );
        }

        if (dto.getFunktion() != null) {
            mitglied.setFunktion(dto.getFunktion());
        }

        if (Boolean.TRUE.equals(dto.getHauptVerein())
                && !mitglied.getHauptVerein()) {

            mitgliedRepository.unsetHauptvereinByPerson(
                    mitglied.getPerson().getId()
            );

            mitglied.setHauptVerein(true);
        }

        if (Boolean.FALSE.equals(dto.getHauptVerein())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A person must always have exactly one Hauptverein"
            );
        }

        return mitgliedMapper.toDTO(mitglied);
    }

}