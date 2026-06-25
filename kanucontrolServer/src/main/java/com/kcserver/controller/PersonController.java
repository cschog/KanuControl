package com.kcserver.controller;

import com.kcserver.dto.person.*;
import com.kcserver.service.PersonService;
import com.kcserver.validation.OnCreate;
import com.kcserver.validation.OnUpdate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import com.kcserver.api.response.ApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import com.kcserver.dto.common.ScrollResponse;


@RestController
@RequestMapping("/api/person")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    /* =========================
       LIST (PAGINIERT)
       ========================= */

    @GetMapping("/scroll")
    public ApiResponse<ScrollResponse<PersonListDTO>> scroll(
            @RequestParam(required = false) String cursorName,
            @RequestParam(required = false) String cursorVorname,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "25") int size,
            @ModelAttribute PersonSearchCriteria criteria
    ) {
        return new ApiResponse<>(
                personService.scroll(
                        cursorName,
                        cursorVorname,
                        cursorId,
                        size,
                        criteria
                ),
                List.of()
        );
    }


    @GetMapping
    public ApiResponse<Page<PersonListDTO>> getAll(Pageable pageable) {

        return ApiResponse.of(
                personService.getAll(pageable)

        );
    }

    /* =========================
   LIST (OHNE PAGING)
   ========================= */

    @GetMapping("/all")
    public ApiResponse<List<PersonListDTO>> getAll(
            @ModelAttribute PersonSearchCriteria criteria,
            Pageable pageable
    ) {

        Pageable mapped = mapSort(pageable);

        return ApiResponse.of(
                personService.getAll(mapped.getSort(), criteria)
        );
    }


    /* =========================
       SEARCH (PAGINIERT)
       ========================= */

    @GetMapping("/search")
    public ApiResponse<Page<PersonListDTO>> search(
            PersonSearchCriteria criteria,
            Pageable pageable
    ) {

        Pageable mapped = mapSort(pageable);

        return ApiResponse.of(
                personService.searchList(criteria, mapped)
        );
    }

    private Pageable mapSort(Pageable pageable) {

        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }

        List<Sort.Order> safeOrders = new ArrayList<>();

        for (Sort.Order order : pageable.getSort()) {

            String p = order.getProperty();

            switch (p) {

                // ✅ echtes DB Feld
                case "name":
                case "vorname":
                case "ort":
                case "plz":
                case "aktiv":
                case "email":
                    safeOrders.add(order);
                    break;

                // ⭐ FE "Name" = fullName → name + vorname
                case "fullName":
                    safeOrders.add(new Sort.Order(order.getDirection(), "name"));
                    safeOrders.add(new Sort.Order(order.getDirection(), "vorname"));
                    break;

                // ⭐ Alter → geburtsdatum invertiert
                case "alter":
                    safeOrders.add(new Sort.Order(
                            order.getDirection() == Sort.Direction.ASC
                                    ? Sort.Direction.DESC
                                    : Sort.Direction.ASC,
                            "geburtsdatum"
                    ));
                    break;

                // ❌ NICHT sortierbare Felder → ignorieren (KEIN Crash!)
                case "mitgliedschaftenCount":
                case "hauptvereinAbk":
                case "vereinAbk":
                    break;

                default:
                    // ❗ unbekannt → ignorieren
                    break;
            }
        }

        Sort safeSort = safeOrders.isEmpty()
                ? Sort.unsorted()
                : Sort.by(safeOrders);

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                safeSort
        );
    }

    /* =========================
       DETAIL
       ========================= */

    @GetMapping("/{id}")
    public ApiResponse<PersonDetailDTO> getPerson(
            @PathVariable long id
    ) {

        return ApiResponse.of(
                personService.getPersonDetail(id)

        );
    }
    /* =========================
       CREATE
       ========================= */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PersonDetailDTO> createPerson(
            @Validated(OnCreate.class)
            @RequestBody PersonSaveDTO dto
    ) {

        return  ApiResponse.of(
                personService.createPerson(dto)
        );
    }

    /* =========================
       UPDATE
       ========================= */

    @PutMapping("/{id}")
    public ApiResponse<PersonDetailDTO> updatePerson(
            @PathVariable long id,
            @Validated(OnUpdate.class)
            @RequestBody PersonSaveDTO dto
    ) {

        return ApiResponse.of(
                personService.updatePerson(id, dto)
        );
    }

    /* =========================
       DELETE
       ========================= */

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePerson(@PathVariable long id) {
        personService.deletePerson(id);
    }

    @GetMapping("/search/ref")
    public ApiResponse<List<PersonRefDTO>> searchRef(
            @RequestParam(required = false) String search
    ) {

        return ApiResponse.of(
                personService.searchRefList(search)
        );
    }
}