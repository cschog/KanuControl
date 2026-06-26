package com.kcserver.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.postalcode.PostalCodeLookupResponse;
import com.kcserver.enumtype.CountryCode;
import com.kcserver.service.postalcode.PostalCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/postal-codes")
@RequiredArgsConstructor
public class PostalCodeController {

    private final PostalCodeService postalCodeService;

    @GetMapping("/lookup")
    public ApiResponse<PostalCodeLookupResponse> lookup(
            @RequestParam CountryCode country,
            @RequestParam String postalCode
    ) {

        PostalCodeLookupResponse response =
                postalCodeService
                        .lookup(country, postalCode)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "PLZ nicht gefunden"
                                ));

        return ApiResponse.of(response);
    }

    @GetMapping("/suggest")
    public ApiResponse<List<PostalCodeLookupResponse>> suggest(
            @RequestParam CountryCode country,
            @RequestParam String query
    ) {

        return ApiResponse.of(
                postalCodeService.suggest(country, query)
        );
    }
}