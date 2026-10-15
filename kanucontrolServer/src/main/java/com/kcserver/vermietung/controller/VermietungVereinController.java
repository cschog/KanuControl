package com.kcserver.vermietung.controller;

import com.kcserver.vermietung.service.VermietungVereinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vermietung/vereine")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'VERMIETUNG')")
public class VermietungVereinController {

    private final VermietungVereinService vermietungVereinService;

    @PutMapping("/{id}/hausverein")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setHausverein(@PathVariable Long id) {
        vermietungVereinService.setHausverein(id);
    }
}