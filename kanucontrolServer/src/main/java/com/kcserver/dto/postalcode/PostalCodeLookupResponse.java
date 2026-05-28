package com.kcserver.dto.postalcode;

import com.kcserver.enumtype.CountryCode;

public record PostalCodeLookupResponse(
        String postalCode,
        String city,
        CountryCode countryCode
) {
}