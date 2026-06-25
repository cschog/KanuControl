package com.kcserver.controller;

import com.kcserver.dto.person.PersonListDTO;
import com.kcserver.dto.teilnehmer.*;
import com.kcserver.service.TeilnehmerService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.kcserver.api.response.ApiResponse;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Set;

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

    public ApiResponse<List<TeilnehmerListDTO>> search(
            @PathVariable Long veranstaltungId,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.of(
                teilnehmerService.search(veranstaltungId, search)
        );
    }

    @GetMapping("/search/ref")

    public ApiResponse<List<TeilnehmerRefDTO>> searchRef(
            @PathVariable Long veranstaltungId,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.of(
                teilnehmerService.searchRef(veranstaltungId, search)

        );
    }

    @GetMapping("/available/paged")

    public ApiResponse<Page<PersonListDTO>> findAvailablePaged(
            @PathVariable Long veranstaltungId,

            @RequestParam(required = false) String search,
            @RequestParam(required = false) String verein,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size,

            @RequestParam(defaultValue = "name") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {

        // =========================
        // SORT FIELD SECURITY
        // =========================

        Set<String> allowedSortFields = Set.of(
                "name",
                "vorname",
                "hauptvereinAbk"
        );

        if ("fullname".equals(sortField)) {
            sortField = "name";
        }

        if (!allowedSortFields.contains(sortField)) {
            sortField = "name";
        }

        // =========================
        // SORT
        // =========================

        Sort.Direction direction =
                sortDirection.equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Sort sort;

        if ("name".equals(sortField)) {

            sort = Sort.by(direction, "name")
                    .and(Sort.by(direction, "vorname"));

        } else {

            sort = Sort.by(direction, sortField);
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        return ApiResponse.of(
                teilnehmerService.findAvailable(
                        veranstaltungId,
                        search,
                        verein,
                        pageable
                )
        );
    }

    @GetMapping("/available")
    public ApiResponse<List<PersonListDTO>> findAvailable(
            @PathVariable Long veranstaltungId,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.of(
                teilnehmerService.findAvailable(veranstaltungId, search)
        );
    }

    /* =========================
       READ
       ========================= */

    @GetMapping("/paged")
    public ApiResponse<Page<TeilnehmerListDTO>> getTeilnehmerPaged(
            @PathVariable Long veranstaltungId,

            TeilnehmerSearchCriteria criteria,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size,

            @RequestParam(defaultValue = "name") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {

        if ("fullname".equals(sortField)) {
            sortField = "person.name";
        }

        Set<String> allowedSortFields = Set.of(
                "person.name",
                "person.vorname",
                "rolle"
        );

        if (!allowedSortFields.contains(sortField)) {
            sortField = "person.name";
        }

        Sort.Direction direction =
                sortDirection.equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Sort sort;

        if ("person.name".equals(sortField)) {

            sort = Sort.by(direction, "person.name")
                    .and(Sort.by(direction, "person.vorname"));

        } else {

            sort = Sort.by(direction, sortField);
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        return ApiResponse.of(
                teilnehmerService.getAssigned(
                        veranstaltungId,
                        criteria,
                        pageable
                )
        );
    }

    @GetMapping
    public ApiResponse<List<PersonListDTO>> getAssigned(
            @PathVariable Long veranstaltungId
    ) {
        return ApiResponse.of(
                teilnehmerService.getAssigned(veranstaltungId)
        );
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
    public ApiResponse<TeilnehmerDetailDTO> addTeilnehmer(
            @PathVariable Long veranstaltungId,
            @PathVariable Long personId
    ) {
        return ApiResponse.of(
                teilnehmerService.addTeilnehmer(
                        veranstaltungId,
                        personId
                )
        );
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
    public ApiResponse<TeilnehmerDetailDTO> setLeiter(
            @PathVariable Long veranstaltungId,
            @PathVariable Long personId
    ) {
        return ApiResponse.of(
                teilnehmerService.setLeiter(
                        veranstaltungId,
                        personId
                )
        );
    }

    @PutMapping("/{teilnehmerId}")
    public ApiResponse<TeilnehmerDetailDTO> updateTeilnehmer(
            @PathVariable Long veranstaltungId,
            @PathVariable Long teilnehmerId,
            @RequestBody TeilnehmerUpdateDTO dto
    ) {
        return ApiResponse.of(
                teilnehmerService.update(
                        veranstaltungId,
                        teilnehmerId,
                        dto
                )
        );
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