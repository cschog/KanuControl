package com.kcserver.controller;

import com.kcserver.dto.postalcode.PostalCodeLookupResponse;
import com.kcserver.enumtype.CountryCode;
import com.kcserver.service.postalcode.PostalCodeService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/postal-codes")
@RequiredArgsConstructor
public class PostalCodeController {

    private final PostalCodeService postalCodeService;

    @GetMapping("/lookup")
    public ResponseEntity<PostalCodeLookupResponse> lookup(
            @RequestParam CountryCode country,
            @RequestParam String postalCode
    ) {

        return postalCodeService
                .lookup(country, postalCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/suggest")
    public List<PostalCodeLookupResponse> suggest(
            @RequestParam CountryCode country,
            @RequestParam String query
    ) {

        return postalCodeService.suggest(country, query);
    }
}