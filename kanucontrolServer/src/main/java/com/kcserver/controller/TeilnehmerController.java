package com.kcserver.controller;

import com.kcserver.dto.person.PersonListDTO;
import com.kcserver.dto.teilnehmer.*;
import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.service.TeilnehmerService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/teilnehmer")
public class TeilnehmerController {

    private final TeilnehmerService teilnehmerService;

    public TeilnehmerController(TeilnehmerService teilnehmerService) {
        this.teilnehmerService = teilnehmerService;
    }

    /* =========================
       SEARCH
       ========================= */

    @GetMapping("/search")
    public List<TeilnehmerListDTO> search(
            @PathVariable Long veranstaltungId,
            @RequestParam(required = false) String search
    ) {
        return teilnehmerService.search(veranstaltungId, search);
    }

    @GetMapping("/search/ref")
    public List<TeilnehmerRefDTO> searchRef(
            @PathVariable Long veranstaltungId,
            @RequestParam(required = false) String search
    ) {
        return teilnehmerService.searchRef(veranstaltungId, search);
    }

    @GetMapping("/available/paged")
    public Page<PersonListDTO> findAvailablePaged(
            @PathVariable Long veranstaltungId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String verein,
            Pageable pageable
    ) {
        return teilnehmerService.findAvailable(
                veranstaltungId,
                search,
                verein,
                pageable
        );
    }

    @GetMapping("/available")
    public List<PersonListDTO> findAvailable(
            @PathVariable Long veranstaltungId,
            @RequestParam(required = false) String search
    ) {
        return teilnehmerService.findAvailable(veranstaltungId, search);
    }

    /* =========================
       READ
       ========================= */

    @GetMapping("/paged")
    public Page<TeilnehmerListDTO> getTeilnehmerPaged(
            @PathVariable Long veranstaltungId,
            TeilnehmerSearchCriteria criteria,
            Pageable pageable
    ) {
        return teilnehmerService.getAssigned(veranstaltungId, criteria, pageable);
    }

    @GetMapping
    public List<PersonListDTO> getAssigned(
            @PathVariable Long veranstaltungId
    ) {
        return teilnehmerService.getAssigned(veranstaltungId);
    }

    /* =========================
       CREATE
       ========================= */

    // ⚠️ WICHTIG: zuerst bulk!
    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public void addTeilnehmerBulk(
            @PathVariable Long veranstaltungId,
            @RequestBody TeilnehmerAddBulkDTO dto
    ) {
        teilnehmerService.addTeilnehmerBulk(
                veranstaltungId,
                dto.getPersonIds()
        );
    }

    @PostMapping("/{personId}")
    @ResponseStatus(HttpStatus.CREATED)
    public TeilnehmerDetailDTO addTeilnehmer(
            @PathVariable Long veranstaltungId,
            @PathVariable Long personId
    ) {
        return teilnehmerService.addTeilnehmer(veranstaltungId, personId);
    }

    /* =========================
       UPDATE
       ========================= */
    @PutMapping("/{personId}/rolle")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRolle(
            @PathVariable Long veranstaltungId,
            @PathVariable Long personId,
            @RequestBody TeilnehmerUpdateDTO dto
    ) {
        teilnehmerService.updateRolle(
                veranstaltungId,
                personId,
                dto.getRolle()
        );
    }

    @PutMapping("/{personId}/leiter")
    public TeilnehmerDetailDTO setLeiter(
            @PathVariable Long veranstaltungId,
            @PathVariable Long personId
    ) {
        return teilnehmerService.setLeiter(veranstaltungId, personId);
    }

    @PutMapping("/{teilnehmerId}")
    public void updateTeilnehmer(
            @PathVariable Long veranstaltungId,
            @PathVariable Long teilnehmerId,
            @RequestBody TeilnehmerUpdateDTO dto
    ) {
        teilnehmerService.update(veranstaltungId, teilnehmerId, dto);
    }

    /* =========================
       DELETE
       ========================= */

    @DeleteMapping("/bulk")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeilnehmerBulk(
            @PathVariable Long veranstaltungId,
            @RequestBody TeilnehmerBulkDeleteDTO dto
    ) {
        teilnehmerService.removeTeilnehmerBulk(
                veranstaltungId,
                dto.getPersonIds()
        );
    }

    @DeleteMapping("/{teilnehmerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeilnehmer(
            @PathVariable Long veranstaltungId,
            @PathVariable Long teilnehmerId
    ) {
        teilnehmerService.removeTeilnehmer(veranstaltungId, teilnehmerId);
    }
}