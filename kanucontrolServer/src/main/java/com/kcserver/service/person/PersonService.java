package com.kcserver.service.person;

import com.kcserver.dto.common.ScrollResponse;
import com.kcserver.dto.person.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
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

    BulkDeleteResultDTO deletePersons(List<Long> ids);

    /* =========================
       SEARCH
       ========================= */
    Page<PersonListDTO> searchList(PersonSearchCriteria criteria, Pageable pageable);

    List<PersonRefDTO> searchRefList(
            String search,
            boolean nurLeiter,
            LocalDate stichtag
    );
}