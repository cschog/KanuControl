package com.kcserver.service.postalcode;

import com.kcserver.dto.postalcode.PostalCodeLookupResponse;
import com.kcserver.enumtype.CountryCode;

import java.util.List;
import java.util.Optional;

public interface PostalCodeProvider {

    Optional<PostalCodeLookupResponse> lookup(
            CountryCode countryCode,
            String postalCode
    );

    default List<PostalCodeLookupResponse> suggest(
            CountryCode countryCode,
            String query
    ) {
        return List.of();
    }
}