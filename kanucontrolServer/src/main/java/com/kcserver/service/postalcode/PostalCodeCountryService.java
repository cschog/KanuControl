package com.kcserver.service.postalcode;

import com.kcserver.dto.postalcode.PostalCodeCountryUpdateRequest;
import com.kcserver.entity.PostalCodeCountry;
import com.kcserver.enumtype.CountryCode;
import com.kcserver.repository.PostalCodeCountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostalCodeCountryService {

    private final PostalCodeCountryRepository repository;

    @Transactional
    public void updateCountry(
            CountryCode countryCode,
            PostalCodeCountryUpdateRequest request
    ) {

        PostalCodeCountry country =
                repository.findById(countryCode)
                        .orElseThrow();

        country.setEnabled(request.enabled());
        country.setAutoImport(request.autoImport());
    }
}
