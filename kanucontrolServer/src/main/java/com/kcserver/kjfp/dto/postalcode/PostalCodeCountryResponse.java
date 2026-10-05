package com.kcserver.kjfp.dto.postalcode;

import com.kcserver.kjfp.enumtype.CountryCode;

import java.time.LocalDateTime;

public record PostalCodeCountryResponse(

        CountryCode countryCode,
        boolean enabled,
        boolean autoImport,
        LocalDateTime lastImport,
        LocalDateTime nextImport
) {}