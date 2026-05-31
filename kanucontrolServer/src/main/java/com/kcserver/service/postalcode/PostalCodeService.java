package com.kcserver.service.postalcode;

import com.kcserver.dto.postalcode.PostalCodeLookupResponse;
import com.kcserver.enumtype.CountryCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PostalCodeService {

    private final PostalCodeProvider provider;

    public Optional<PostalCodeLookupResponse> lookup(
            CountryCode countryCode,
            String postalCode
    ) {
        return provider.lookup(countryCode, postalCode);
    }
    public List<PostalCodeLookupResponse> suggest(
            CountryCode countryCode,
            String query
    ) {
        return provider.suggest(countryCode, query);
    }
}