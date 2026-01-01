package com.kcserver.service;

import com.kcserver.dto.PersonDTO;
import com.kcserver.dto.PersonSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PersonService {

    // READ
    List<PersonDTO> getAllPersons();
    PersonDTO getPerson(long id);

    // CREATE
    PersonDTO createPerson(PersonDTO personDTO);

    // UPDATE
    PersonDTO updatePerson(long id, PersonDTO personDTO);

    // DELETE
    void deletePerson(long id);

    // SEARCH (UC-P1)
    Page<PersonDTO> search(PersonSearchCriteria criteria, Pageable pageable);
}