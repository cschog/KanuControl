package com.kcserver.kjfp.dto.postalcode;

import com.kcserver.kjfp.enumtype.CountryCode;

public record PostalCodeLookupResponse(
        String postalCode,
        String city,
        CountryCode countryCode
) {
}