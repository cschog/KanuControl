package com.kcserver.vermietung.controller;
import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.service.BuchungFinanzenService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/vermietung/finanzen") @PreAuthorize("hasAnyRole('ADMIN','VERMIETUNG')") @Tag(name="Vermietungsfinanzen") @SecurityRequirement(name="bearerAuth")
public class BuchungFinanzenController {
 private final BuchungFinanzenService service;
 public BuchungFinanzenController(BuchungFinanzenService service){this.service=service;}
 @GetMapping public ApiResponse<List<BuchungFinanzenDTO>> getAll(){return new ApiResponse<>(service.getAll(),List.of());}
 @PostMapping("/{buchungId}/bewegungen") public ApiResponse<BuchungFinanzenDTO> add(@PathVariable Long buchungId,@RequestBody @Valid FinanzbewegungSaveDTO dto){return new ApiResponse<>(service.add(buchungId,dto),List.of());}
}
