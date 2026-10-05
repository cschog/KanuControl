package com.kcserver.kjfp.dto.postalcode;

import com.kcserver.kjfp.enumtype.CountryCode;

import java.time.LocalDateTime;

public record PostalCodeStatusResponse(
        CountryCode countryCode,
        long count,
        LocalDateTime lastImport,
        String source,
        String importStatus
) {
}