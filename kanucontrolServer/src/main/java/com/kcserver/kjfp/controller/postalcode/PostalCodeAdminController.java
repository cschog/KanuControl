package com.kcserver.kjfp.controller.postalcode;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.kjfp.dto.postalcode.PostalCodeCountryResponse;
import com.kcserver.kjfp.dto.postalcode.PostalCodeCountryUpdateRequest;
import com.kcserver.kjfp.dto.postalcode.PostalCodeStatusResponse;
import com.kcserver.kjfp.enumtype.CountryCode;
import com.kcserver.kjfp.service.postalcode.PostalCodeCountryService;
import com.kcserver.kjfp.service.postalcode.PostalCodeImportAsyncService;
import com.kcserver.kjfp.service.postalcode.PostalCodeImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/admin/postal-codes")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class PostalCodeAdminController {

    private final PostalCodeImportService importService;
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
    public ApiResponse<List<PostalCodeCountryResponse>> countries() {
        return ApiResponse.of(
                countryService.getCountries()
        );
    }
    @PutMapping("/countries/{countryCode}")
    public void updateCountry(
            @PathVariable CountryCode countryCode,
            @RequestBody PostalCodeCountryUpdateRequest request
    ) {
        countryService.updateCountry(countryCode, request);
    }
}