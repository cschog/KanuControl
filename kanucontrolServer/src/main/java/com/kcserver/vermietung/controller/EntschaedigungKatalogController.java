package com.kcserver.vermietung.controller;
import com.kcserver.vermietung.dto.EntschaedigungKatalogDTO;
import com.kcserver.vermietung.service.EntschaedigungKatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequiredArgsConstructor
@RequestMapping("/api/vermietung/objekte/{objektId}/entschaedigungen")
@PreAuthorize("hasAnyRole('ADMIN','VERMIETUNG')")
public class EntschaedigungKatalogController {
 private final EntschaedigungKatalogService service;
 @GetMapping public List<EntschaedigungKatalogDTO> list(@PathVariable Long objektId){return service.list(objektId);}
 @PostMapping public EntschaedigungKatalogDTO create(@PathVariable Long objektId,@Valid @RequestBody EntschaedigungKatalogDTO dto){return service.save(objektId,null,dto);}
 @PutMapping("/{id}") public EntschaedigungKatalogDTO update(@PathVariable Long objektId,@PathVariable Long id,@Valid @RequestBody EntschaedigungKatalogDTO dto){return service.save(objektId,id,dto);}
}
