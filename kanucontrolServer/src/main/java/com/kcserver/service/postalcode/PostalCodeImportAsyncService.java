package com.kcserver.service.postalcode;

import com.kcserver.enumtype.CountryCode;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostalCodeImportAsyncService {

    private final PostalCodeImportService importService;

    @Async
    public void importCountryAsync(
            CountryCode countryCode
    ) {
        importService.importCountry(countryCode);
    }
}
