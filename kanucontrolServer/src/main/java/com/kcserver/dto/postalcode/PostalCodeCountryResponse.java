package com.kcserver.dto.postalcode;

import com.kcserver.enumtype.CountryCode;

import java.time.LocalDateTime;

public record PostalCodeCountryResponse(

        CountryCode countryCode,
        boolean enabled,
        boolean autoImport,
        LocalDateTime lastImport,
        LocalDateTime nextImport
) {}