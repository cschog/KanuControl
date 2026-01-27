package com.kcserver.service;

import com.kcserver.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PersonService {

    /* =========================
       LIST
       ========================= */
    Page<PersonListDTO> getAll(Pageable pageable);

    /* =========================
       DETAIL
       ========================= */
    PersonDetailDTO getPersonDetail(long id);

    /* =========================
       CREATE / UPDATE / DELETE
       ========================= */
    PersonDetailDTO createPerson(PersonSaveDTO dto);

    PersonDetailDTO updatePerson(long id, PersonSaveDTO dto);

    void deletePerson(long id);

    /* =========================
       SEARCH
       ========================= */
    Page<PersonListDTO> searchList(PersonSearchCriteria criteria, Pageable pageable);
}