package com.kcserver.service;

import com.kcserver.dto.PersonDetailDTO;
import com.kcserver.dto.PersonListDTO;
import com.kcserver.dto.PersonSaveDTO;
import com.kcserver.dto.PersonSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PersonService {

    /* =======================
       LIST / SEARCH
       ======================= */

    List<PersonListDTO> getAllPersonsList();

    Page<PersonListDTO> searchList(
            PersonSearchCriteria criteria,
            Pageable pageable
    );

    /* =======================
       DETAIL
       ======================= */

    PersonDetailDTO getPersonDetail(long id);

    /* =======================
       CREATE / UPDATE
       ======================= */

    PersonDetailDTO createPerson(PersonSaveDTO dto);

    PersonDetailDTO updatePerson(long id, PersonSaveDTO dto);

    /* =======================
       DELETE
       ======================= */

    void deletePerson(long id);
}