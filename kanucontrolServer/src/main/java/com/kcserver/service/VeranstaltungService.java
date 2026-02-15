package com.kcserver.service;

import com.kcserver.dto.person.PersonListDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungCreateDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungDetailDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungListDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungUpdateDTO;
import com.kcserver.enumtype.VeranstaltungTyp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

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
    public VeranstaltungDetailDTO create(VeranstaltungCreateDTO dto);

    /* =========================================================
       READ
       ========================================================= */

    /**
     * Liefert alle Veranstaltungen (Historie).
     */
    Page<VeranstaltungListDTO> getAll(
            String name,
            Boolean aktiv,
            Long vereinId,
            LocalDate beginnVon,
            LocalDate beginnBis,
            VeranstaltungTyp typ,
            Pageable pageable
    );

    Page<VeranstaltungListDTO> getAll(Pageable pageable);

    /**
     * Liefert die aktuell aktive Veranstaltung.
     * @throws 404 wenn keine aktiv ist
     */
    VeranstaltungDetailDTO getActive();

    /**
     * Liefert eine Veranstaltung per ID.
     */
    VeranstaltungDetailDTO getById(Long id);

    /* =========================================================
       AVAILABE
       ========================================================= */

    Page<PersonListDTO> getAvailablePersons(
            Long veranstaltungId,
            String name,
            String vorname,
            String verein,
            Pageable pageable
    );

    List<PersonListDTO> getAssignedPersons(Long veranstaltungId);

    void addTeilnehmerBulk(Long veranstaltungId, List<Long> personIds);

    void removeTeilnehmerBulk(Long veranstaltungId, List<Long> personIds);

     /* =========================================================
       UPDATE
       ========================================================= */
     VeranstaltungDetailDTO update(
             Long id,
             VeranstaltungUpdateDTO dto
     );

    VeranstaltungDetailDTO setActive(Long id);

    /* =========================================================
       DELETE
       ========================================================= */

    void delete(Long id);

}