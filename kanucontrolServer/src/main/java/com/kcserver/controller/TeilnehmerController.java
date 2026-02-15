package com.kcserver.controller;

import com.kcserver.dto.person.PersonListDTO;
import com.kcserver.dto.teilnehmer.*;
import com.kcserver.service.TeilnehmerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veranstaltung/{veranstaltungId}/teilnehmer")
public class TeilnehmerController {

    private final TeilnehmerService teilnehmerService;

    public TeilnehmerController(TeilnehmerService teilnehmerService) {
        this.teilnehmerService = teilnehmerService;
    }

    /* =========================================================
       READ (Paging)
       ========================================================= */

    @GetMapping
    public Page<TeilnehmerListDTO> getTeilnehmer(
            @PathVariable Long veranstaltungId,
            @PageableDefault(size = 20, sort = "person.name") Pageable pageable
    ) {
        return teilnehmerService.getTeilnehmer(veranstaltungId, pageable);
    }

    /* =========================================================
       AVAILABLE PERSONS (nicht Teilnehmer)
       ========================================================= */

    @GetMapping("/available")
    public Page<PersonListDTO> getAvailablePersons(
            @PathVariable Long veranstaltungId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String vorname,
            @RequestParam(required = false) String verein,
            Pageable pageable
    ) {
        return teilnehmerService.getAvailablePersons(veranstaltungId, name, vorname, verein, pageable);
    }

    /* =========================================================
       ASSIGNED PERSONS (alle Teilnehmer ohne Paging)
       ========================================================= */

    @GetMapping("/assigned")
    public List<PersonListDTO> getAssigned(
            @PathVariable Long veranstaltungId
    ) {
        return teilnehmerService.getAssignedPersons(veranstaltungId);
    }

    /* =========================================================
       CREATE (single)
       ========================================================= */

    @PostMapping("/{personId}")
    @ResponseStatus(HttpStatus.CREATED)
    public TeilnehmerDetailDTO addTeilnehmer(
            @PathVariable Long veranstaltungId,
            @PathVariable Long personId
    ) {
        return teilnehmerService.addTeilnehmer(veranstaltungId, personId);
    }

    /* =========================================================
       CREATE (bulk)
       ========================================================= */

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public void addBulk(
            @PathVariable Long veranstaltungId,
            @RequestBody TeilnehmerAddBulkDTO dto
    ) {
        teilnehmerService.addTeilnehmerBulk(veranstaltungId, dto.getPersonIds());
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    @PutMapping("/{teilnehmerId}")
    public TeilnehmerDetailDTO update(
            @PathVariable Long veranstaltungId,
            @PathVariable Long teilnehmerId,
            @RequestBody TeilnehmerUpdateDTO dto
    ) {
        return teilnehmerService.update(veranstaltungId, teilnehmerId, dto);
    }

    /* =========================================================
       DELETE (single)
       ========================================================= */

    @DeleteMapping("/{teilnehmerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTeilnehmer(
            @PathVariable Long veranstaltungId,
            @PathVariable Long teilnehmerId
    ) {
        teilnehmerService.removeTeilnehmer(veranstaltungId, teilnehmerId);
    }

    /* =========================================================
       DELETE (bulk)
       ========================================================= */

    @DeleteMapping("/bulk")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTeilnehmerBulk(
            @PathVariable Long veranstaltungId,
            @Valid @RequestBody TeilnehmerBulkDeleteDTO dto
    ) {
        teilnehmerService.removeTeilnehmerBulk(veranstaltungId, dto.getPersonIds());
    }

    /* =========================================================
       SET LEITER
       ========================================================= */

    @PutMapping("/{personId}/leiter")
    @ResponseStatus(HttpStatus.OK)
    public TeilnehmerDetailDTO setLeiter(
            @PathVariable Long veranstaltungId,
            @PathVariable Long personId
    ) {
        return teilnehmerService.setLeiter(veranstaltungId, personId);
    }
}