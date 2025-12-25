package com.kcserver.service;

import com.kcserver.dto.PersonDTO;
import com.kcserver.entity.Person;
import com.kcserver.mapper.EntityMapper;
import com.kcserver.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonService {

    private static final Logger logger = LoggerFactory.getLogger(PersonService.class);

    private final PersonRepository personRepository;
    private final EntityMapper mapper;

    @Autowired
    public PersonService(PersonRepository personRepository, EntityMapper mapper) {
        this.personRepository = personRepository;
        this.mapper = mapper;
    }

    /* =========================================================
       READ
       ========================================================= */

    @Transactional(readOnly = true)
    public List<PersonDTO> getAllPersons() {
        logger.debug("Fetching all persons");
        return personRepository.findAll().stream()
                .map(mapper::toPersonDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PersonDTO getPerson(long id) {
        logger.debug("Fetching person with id {}", id);
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Person not found"));
        return mapper.toPersonDTO(person);
    }

    /* =========================================================
       CREATE
       ========================================================= */

    @Transactional
    public PersonDTO createPerson(PersonDTO personDTO) {
        logger.info("Creating new person: {}", personDTO);

        Person person = mapper.toPersonEntity(personDTO);
        Person savedPerson = personRepository.save(person);

        logger.info("Person created with id {}", savedPerson.getId());
        return mapper.toPersonDTO(savedPerson);
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    @Transactional
    public PersonDTO updatePerson(long id, PersonDTO personDTO) {
        logger.info("Updating person with id {}", id);

        Person existingPerson = personRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Person not found"));

        mapper.updatePersonFromDTO(personDTO, existingPerson);

        Person updatedPerson = personRepository.save(existingPerson);

        logger.info("Person updated with id {}", id);
        return mapper.toPersonDTO(updatedPerson);
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @Transactional
    public void deletePerson(long id) {
        logger.info("Deleting person with id {}", id);

        if (!personRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Person not found");
        }

        personRepository.deleteById(id);
        logger.info("Person deleted with id {}", id);
    }
}