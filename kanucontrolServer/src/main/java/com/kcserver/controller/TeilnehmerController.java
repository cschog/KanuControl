package com.kcserver.controller;

import com.kcserver.dto.TeilnehmerDTO;
import com.kcserver.service.TeilnehmerService;
import com.kcserver.tenancy.TenantContext;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/teilnehmer")
public class TeilnehmerController {

    private static final Logger logger =
            LoggerFactory.getLogger(TeilnehmerController.class);

    private final TeilnehmerService teilnehmerService;

    public TeilnehmerController(TeilnehmerService teilnehmerService) {
        this.teilnehmerService = teilnehmerService;
    }

    /* =========================================================
       READ
       ========================================================= */

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TeilnehmerDTO>> getTeilnehmer(
            @PathVariable Long veranstaltungId) {

        logger.debug(
                "GET Teilnehmer | veranstaltung={} tenant={}",
                veranstaltungId,
                TenantContext.getTenant()
        );

        return ResponseEntity.ok(
                teilnehmerService.getTeilnehmerByVeranstaltung(veranstaltungId)
        );
    }

    /* =========================================================
       CREATE
       ========================================================= */

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TeilnehmerDTO> addTeilnehmer(
            @PathVariable Long veranstaltungId,
            @Valid @RequestBody TeilnehmerDTO dto) {

        logger.info(
                "ADD Teilnehmer | veranstaltung={} person={} tenant={}",
                veranstaltungId,
                dto.getPersonId(),
                TenantContext.getTenant()
        );

        TeilnehmerDTO created =
                teilnehmerService.addTeilnehmer(veranstaltungId, dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @DeleteMapping("/{personId}")
    public ResponseEntity<Void> removeTeilnehmer(
            @PathVariable Long veranstaltungId,
            @PathVariable Long personId) {

        logger.info(
                "REMOVE Teilnehmer | veranstaltung={} person={} tenant={}",
                veranstaltungId,
                personId,
                TenantContext.getTenant()
        );

        teilnehmerService.removeTeilnehmer(veranstaltungId, personId);

        return ResponseEntity.noContent().build();
    }

    /* =========================================================
       LEITER
       ========================================================= */

    @PutMapping(
            value = "/leiter/{personId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TeilnehmerDTO> setLeiter(
            @PathVariable Long veranstaltungId,
            @PathVariable Long personId) {

        logger.info(
                "SET Leiter | veranstaltung={} person={} tenant={}",
                veranstaltungId,
                personId,
                TenantContext.getTenant()
        );

        TeilnehmerDTO leiter =
                teilnehmerService.setLeiter(veranstaltungId, personId);

        return ResponseEntity.ok(leiter);
    }
}