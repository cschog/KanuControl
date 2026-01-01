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
import com.kcserver.tenancy.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        logger.info(">>> Current tenant BEFORE save: {}",
                TenantContext.getCurrentTenant());

        // 1️⃣ Person speichern (OHNE Mitgliedschaften)
        Person person = personMapper.toEntity(dto);
        Person savedPerson = personRepository.save(person);

        // 2️⃣ Mitgliedschaften explizit anlegen
        if (dto.getMitgliedschaften() != null) {
            for (MitgliedDTO m : dto.getMitgliedschaften()) {

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
                mitglied.setHauptVerein(m.getHauptVerein());

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

        personMapper.updateFromDTO(dto, existing);
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
                PersonSpecification.byCriteria(
                        criteria.getName(),
                        criteria.getVorname(),
                        criteria.getVereinId(),
                        criteria.getAktiv(),
                        criteria.getSex(),
                        criteria.getAlterMin(),
                        criteria.getAlterMax(),
                        criteria.getPlz(),
                        criteria.getOrt()
                ),
                pageable
        ).map(personMapper::toDTO);
    }
}