package com.kcserver.service;

import com.kcserver.dto.PersonDTO;
import com.kcserver.entity.Person;
import com.kcserver.mapper.PersonMapper;
import com.kcserver.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PersonService {

    private static final Logger logger =
            LoggerFactory.getLogger(PersonService.class);

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    public PersonService(
            PersonRepository personRepository,
            PersonMapper personMapper
    ) {
        this.personRepository = personRepository;
        this.personMapper = personMapper;
    }

    /* =========================================================
       READ
       ========================================================= */

    @Transactional(readOnly = true)
    public List<PersonDTO> getAllPersons() {
        logger.debug("Fetching all persons");

        return personRepository.findAll()
                .stream()
                .map(personMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PersonDTO getPerson(long id) {
        logger.debug("Fetching person with id {}", id);

        Person person = personRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Person not found"
                        )
                );

        return personMapper.toDTO(person);
    }

    /* =========================================================
       CREATE
       ========================================================= */

    @Transactional
    public PersonDTO createPerson(PersonDTO dto) {
        logger.info("Creating new person");

        Person person = personMapper.toEntity(dto);
        Person saved = personRepository.save(person);

        logger.info("Person created with id {}", saved.getId());
        return personMapper.toDTO(saved);
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    @Transactional
    public PersonDTO updatePerson(long id, PersonDTO dto) {
        logger.info("Updating person with id {}", id);

        Person person = personRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Person not found"
                        )
                );

        personMapper.updateFromDTO(dto, person);

        Person updated = personRepository.save(person);

        logger.info("Person updated with id {}", id);
        return personMapper.toDTO(updated);
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @Transactional
    public void deletePerson(long id) {
        logger.info("Deleting person with id {}", id);

        if (!personRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Person not found"
            );
        }

        personRepository.deleteById(id);
        logger.info("Person deleted with id {}", id);
    }
}