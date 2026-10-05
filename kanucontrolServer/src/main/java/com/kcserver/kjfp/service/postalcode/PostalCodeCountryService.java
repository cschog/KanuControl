package com.kcserver.kjfp.service.postalcode;

import com.kcserver.kjfp.dto.postalcode.PostalCodeCountryResponse;
import com.kcserver.kjfp.dto.postalcode.PostalCodeCountryUpdateRequest;
import com.kcserver.kjfp.entity.postalcode.PostalCodeCountry;
import com.kcserver.kjfp.enumtype.CountryCode;
import com.kcserver.kjfp.repository.postalcode.PostalCodeCountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostalCodeCountryService {

    private final PostalCodeCountryRepository repository;

    @Transactional(readOnly = true)
    public List<PostalCodeCountryResponse> getCountries() {

        return repository.findAll()
                .stream()
                .map(c -> new PostalCodeCountryResponse(
                        c.getCountryCode(),
                        c.isEnabled(),
                        c.isAutoImport(),
                        c.getLastImport(),
                        c.getNextImport()
                ))
                .toList();
    }

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
