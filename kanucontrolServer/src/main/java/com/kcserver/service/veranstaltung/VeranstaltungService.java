package com.kcserver.service.veranstaltung;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.beitrag.BeitragsstrukturDTO;
import com.kcserver.dto.person.PersonListDTO;
import com.kcserver.dto.veranstaltung.*;
import com.kcserver.entity.Veranstaltung;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;


public interface VeranstaltungService {

    /* =========================================================
       CREATE
       ========================================================= */

    /**
     * Legt eine neue Veranstaltung an.
     * - genau ein veranstaltender Verein
     * - genau ein Leiter (>= 18 Jahre)
     * - Veranstaltung wird automatisch aktiv
     * - Leiter wird automatisch als Teilnehmer (LEITER) angelegt
     */
    ApiResponse<VeranstaltungDetailDTO> create(

            VeranstaltungCreateDTO dto);

    /* =========================================================
       READ
       ========================================================= */

    /**
     * Liefert die aktuell aktive Veranstaltung.
     **/
    VeranstaltungDetailDTO getActive();

    /**
     * Liefert eine Veranstaltung per ID.
     */
    VeranstaltungDetailDTO getById(Long id);

    Page<VeranstaltungListDTO> search(
            VeranstaltungFilterDTO filter,
            Pageable pageable
    );
    /* =========================================================
       AVAILABLE
       ========================================================= */

    List<PersonListDTO> getAssignedPersons(Long veranstaltungId);

    void addTeilnehmerBulk(Long veranstaltungId, List<Long> personIds);

    void removeTeilnehmerBulk(Long veranstaltungId, List<Long> personIds);

     /* =========================================================
       UPDATE
       ========================================================= */
     ApiResponse<VeranstaltungDetailDTO> update(
             Long id,
             VeranstaltungUpdateDTO dto);

    VeranstaltungDetailDTO setActive(Long id);

    /* =========================================================
       DELETE
       ========================================================= */

    void delete(Long id);

    Optional<VeranstaltungDetailDTO> getActiveOptional();

    List<VeranstaltungListDTO> getAll();

    List<PersonListDTO> getAvailablePersons(Long veranstaltungId, String search);

    List<VeranstaltungListDTO> searchAll(
            VeranstaltungFilterDTO filter
    );

    Veranstaltung findEntityById(Long id);

    BeitragsstrukturDTO assignBeitragsstrukturFromTemplate(
            Long veranstaltungId,
            Long templateId
    );
}