package com.kcserver.vermietung.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.dto.VeranstalterDTO;
import com.kcserver.vermietung.service.VeranstalterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/vermietung/veranstalter")
@PreAuthorize("hasAnyRole('ADMIN', 'VERMIETUNG')")
public class VeranstalterController {
    private final VeranstalterService service;
    @GetMapping public ApiResponse<List<VeranstalterDTO>> suchen(@RequestParam(required=false) String search){return ApiResponse.of(service.suchen(search));}
    @GetMapping("/{id}") public ApiResponse<VeranstalterDTO> lesen(@PathVariable Long id){return ApiResponse.of(service.lesen(id));}
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<VeranstalterDTO> anlegen(@RequestBody @Valid VeranstalterDTO dto){return ApiResponse.of(service.speichern(null,dto));}
    @PutMapping("/{id}") public ApiResponse<VeranstalterDTO> speichern(@PathVariable Long id,@RequestBody @Valid VeranstalterDTO dto){return ApiResponse.of(service.speichern(id,dto));}
}
