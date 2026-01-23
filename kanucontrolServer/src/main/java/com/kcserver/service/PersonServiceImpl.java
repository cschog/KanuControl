package com.kcserver.service;

import com.kcserver.dto.MitgliedDTO;
import com.kcserver.dto.PersonDTO;
import com.kcserver.dto.PersonSearchCriteria;
import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.mapper.PersonMapper;
import com.kcserver.persistence.specification.PersonSpecification;
import com.kcserver.repository.MitgliedRepository;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VereinRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional
public class PersonServiceImpl implements PersonService {

    private static final Logger logger =
            LoggerFactory.getLogger(PersonServiceImpl.class);

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final VereinRepository vereinRepository;
    private final MitgliedRepository mitgliedRepository;

    public PersonServiceImpl(
            PersonRepository personRepository,
            PersonMapper personMapper,
            MitgliedRepository mitgliedRepository,
            VereinRepository vereinRepository
    ) {
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.mitgliedRepository = mitgliedRepository;
        this.vereinRepository = vereinRepository;
    }

    /* =========================================================
       READ
       ========================================================= */

    @Override
    @Transactional(readOnly = true)
    public List<PersonDTO> getAllPersons() {
        return personRepository.findAll()
                .stream()
                .map(personMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PersonDTO getPerson(long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "Person not found"
                ));
        return personMapper.toDTO(person);
    }

    /* =========================================================
       CREATE
       ========================================================= */

    @Override
    public PersonDTO createPerson(PersonDTO dto) {

        // 0️⃣ Fachliche Duplikatsprüfung
        if (dto.getGeburtsdatum() != null) {

            boolean exists = personRepository
                    .existsByVornameAndNameAndGeburtsdatum(
                            dto.getVorname(),
                            dto.getName(),
                            dto.getGeburtsdatum()
                    );

            if (exists) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Eine Person mit gleichem Namen und Geburtsdatum existiert bereits"
                );
            }
        }

        // 1️⃣ Person speichern (OHNE Mitgliedschaften)
        Person person = personMapper.toEntity(dto);
        Person savedPerson = personRepository.save(person);

        // 2️⃣ Mitgliedschaften explizit anlegen
        if (dto.getMitgliedschaften() != null) {
            for (MitgliedDTO m : dto.getMitgliedschaften()) {

                // ✅ KEIN Verein → KEINE Mitgliedschaft
                if (m.getVereinId() == null) {
                    continue;
                }

                Mitglied mitglied = new Mitglied();
                mitglied.setPerson(savedPerson);

                Verein verein = vereinRepository.findById(m.getVereinId())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Verein not found: " + m.getVereinId()
                                )
                        );

                mitglied.setVerein(verein);
                mitglied.setFunktion(m.getFunktion());
                mitglied.setHauptVerein(Boolean.TRUE.equals(m.getHauptVerein()));

                mitgliedRepository.save(mitglied);
            }
        }

        return personMapper.toDTO(savedPerson);
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    @Override
    public PersonDTO updatePerson(long id, PersonDTO dto) {

        Person existing = personRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "Person not found"
                ));

        // 0️⃣ Fachliche Duplikatsprüfung (nur wenn Geburtsdatum gesetzt)
        if (dto.getGeburtsdatum() != null) {
            personRepository
                    .findByVornameAndNameAndGeburtsdatum(
                            dto.getVorname(),
                            dto.getName(),
                            dto.getGeburtsdatum()
                    )
                    .filter(p -> !p.getId().equals(existing.getId())) // 👈 sich selbst erlauben
                    .ifPresent(p -> {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Eine andere Person mit gleichem Namen und Geburtsdatum existiert bereits"
                        );
                    });
        }

        // 1️⃣ Update durchführen
        personMapper.updateFromDTO(dto, existing);

        // 2️⃣ Rückgabe
        return personMapper.toDTO(existing);
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @Override
    public void deletePerson(long id) {
        if (!personRepository.existsById(id)) {
            throw new ResponseStatusException(
                    NOT_FOUND, "Person not found"
            );
        }
        personRepository.deleteById(id);
    }

    /* =========================================================
       SEARCH (UC-P1)
       ========================================================= */

    @Override
    @Transactional(readOnly = true)
    public Page<PersonDTO> search(PersonSearchCriteria criteria, Pageable pageable) {

        return personRepository.findAll(
                PersonSpecification.byCriteria(criteria),
                pageable
        ).map(personMapper::toDTO);
    }
}