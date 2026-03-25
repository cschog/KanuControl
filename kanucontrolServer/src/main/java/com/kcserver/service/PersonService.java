package com.kcserver.service;

import com.kcserver.dto.common.ScrollResponse;
import com.kcserver.dto.person.PersonDetailDTO;
import com.kcserver.dto.person.PersonListDTO;
import com.kcserver.dto.person.PersonSaveDTO;
import com.kcserver.dto.person.PersonSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.kcserver.dto.person.PersonRefDTO;

import java.util.List;

public interface PersonService {

    /* =========================
       LIST
       ========================= */
    ScrollResponse<PersonListDTO> scroll(
            String cursorName,
            String cursorVorname,
            Long cursorId,
            int size,
            PersonSearchCriteria criteria
    );

    Page<PersonListDTO> getAll(Pageable pageable);

    List<PersonListDTO> getAll(Sort sort, PersonSearchCriteria criteria);
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

    List<PersonRefDTO> searchRefList(String search);
}