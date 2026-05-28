package com.kcserver.service.postalcode;

import com.kcserver.dto.postalcode.PostalCodeLookupResponse;
import com.kcserver.enumtype.CountryCode;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class StaticPostalCodeProvider implements PostalCodeProvider {

    private static final Map<String, String> DE_DATA = Map.of(
            "50226", "Frechen",
            "52249", "Eschweiler",
            "46049", "Oberhausen"
    );

    @Override
    public Optional<PostalCodeLookupResponse> lookup(
            CountryCode countryCode,
            String postalCode
    ) {

        if (countryCode != CountryCode.DE) {
            return Optional.empty();
        }

        String city = DE_DATA.get(postalCode);

        if (city == null) {
            return Optional.empty();
        }

        return Optional.of(
                new PostalCodeLookupResponse(
                        postalCode,
                        city,
                        countryCode
                )
        );
    }

}