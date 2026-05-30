package com.kcserver.dto.postalcode;

import com.kcserver.enumtype.CountryCode;

import java.time.LocalDateTime;

public record PostalCodeStatusResponse(
        CountryCode countryCode,
        long count,
        LocalDateTime lastImport,
        String source,
        String importStatus
) {
}