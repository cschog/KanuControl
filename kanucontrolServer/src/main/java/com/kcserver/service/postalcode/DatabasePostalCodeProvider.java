package com.kcserver.service.postalcode;

import com.kcserver.dto.postalcode.PostalCodeLookupResponse;
import com.kcserver.entity.PostalCode;
import com.kcserver.enumtype.CountryCode;
import com.kcserver.repository.PostalCodeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Primary
@RequiredArgsConstructor
public class DatabasePostalCodeProvider implements PostalCodeProvider {

    private final PostalCodeRepository repository;

    @Override
    public Optional<PostalCodeLookupResponse> lookup(
            CountryCode countryCode,
            String postalCode
    ) {

        return repository
                .findFirstByCountryCodeAndPostalCode(
                        countryCode,
                        postalCode
                )
                .map(this::toResponse);
    }

    @Override
    public List<PostalCodeLookupResponse> suggest(
            CountryCode countryCode,
            String query
    ) {

        if (query == null || query.isBlank()) {
            return List.of();
        }

        // PLZ-Suche
        if (query.matches("\\d+")) {

            return repository
                    .findTop200ByCountryCodeAndPostalCodeStartingWithOrderByPostalCode(
                            countryCode,
                            query
                    )
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        // Ortssuche
        return repository
                .findTop200ByCountryCodeAndCityContainingIgnoreCaseOrderByCity(
                        countryCode,
                        query
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PostalCodeLookupResponse toResponse(
            PostalCode postalCode
    ) {

        return new PostalCodeLookupResponse(
                postalCode.getPostalCode(),
                postalCode.getCity(),
                postalCode.getCountryCode()
        );
    }
}