package com.kcserver.controller;

import com.kcserver.dto.teilnehmer.TeilnehmerBulkDeleteDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerDetailDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerListDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerUpdateDTO;
import com.kcserver.service.TeilnehmerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
       CREATE
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
       LEITER
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