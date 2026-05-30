package com.kcserver.controller;

import com.kcserver.dto.postalcode.PostalCodeStatusResponse;
import com.kcserver.enumtype.CountryCode;
import com.kcserver.service.postalcode.PostalCodeImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/postal-codes")
@RequiredArgsConstructor
public class PostalCodeAdminController {

    private final PostalCodeImportService importService;

    @PostMapping("/import/{countryCode}")
    public ResponseEntity<String> importCountry(
            @PathVariable CountryCode countryCode
    ) {
        importService.importCountry(countryCode);
        return ResponseEntity.ok("Import finished");
    }

    @GetMapping("/status/{countryCode}")
    public PostalCodeStatusResponse status(
            @PathVariable CountryCode countryCode
    ) {
        return importService.getStatus(countryCode);
    }

    @GetMapping("/countries")
    public CountryCode[] countries() {
        return CountryCode.values();
    }
}