package com.kcserver.controller;

import com.kcserver.dto.postalcode.PostalCodeCountryResponse;
import com.kcserver.dto.postalcode.PostalCodeCountryUpdateRequest;
import com.kcserver.dto.postalcode.PostalCodeStatusResponse;
import com.kcserver.enumtype.CountryCode;
import com.kcserver.repository.PostalCodeCountryRepository;
import com.kcserver.service.postalcode.PostalCodeCountryService;
import com.kcserver.service.postalcode.PostalCodeImportAsyncService;
import com.kcserver.service.postalcode.PostalCodeImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/postal-codes")
@RequiredArgsConstructor
public class PostalCodeAdminController {

    private final PostalCodeImportService importService;
    private final PostalCodeCountryRepository countryRepository;
    private final PostalCodeCountryService countryService;
    private final PostalCodeImportAsyncService asyncService;

    @PostMapping("/import/{countryCode}")
    public ResponseEntity<String> importCountry(
            @PathVariable CountryCode countryCode
    ) {

        asyncService.importCountryAsync(countryCode);

        return ResponseEntity.accepted()
                .body("Import started");
    }

    @GetMapping("/status/{countryCode}")
    public PostalCodeStatusResponse status(
            @PathVariable CountryCode countryCode
    ) {
        return importService.getStatus(countryCode);
    }

    @GetMapping("/countries")
    public List<PostalCodeCountryResponse> countries() {

        return countryRepository.findAll()
                .stream()
                .map(c -> new PostalCodeCountryResponse(
                        c.getCountryCode(),
                        c.isEnabled(),
                        c.isAutoImport(),
                        c.getLastImport(),
                        c.getNextImport()
                ))
                .toList();
    }
    @PutMapping("/countries/{countryCode}")
    public void updateCountry(
            @PathVariable CountryCode countryCode,
            @RequestBody PostalCodeCountryUpdateRequest request
    ) {
        countryService.updateCountry(countryCode, request);
    }
}