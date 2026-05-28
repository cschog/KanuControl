package com.kcserver.service.postalcode;

import com.kcserver.dto.postalcode.PostalCodeLookupResponse;
import com.kcserver.enumtype.CountryCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Service
@Primary
@RequiredArgsConstructor
public class ZippopotamPostalCodeProvider
        implements PostalCodeProvider {

    private final RestClient restClient;

    @Override
    public Optional<PostalCodeLookupResponse> lookup(
            CountryCode countryCode,
            String postalCode
    ) {

        if (countryCode != CountryCode.DE) {
            return Optional.empty();
        }

        try {

            ZippopotamResponse response =
                    restClient.get()
                            .uri(
                                    "https://api.zippopotam.us/de/{plz}",
                                    postalCode
                            )
                            .retrieve()
                            .body(ZippopotamResponse.class);

            if (response == null
                    || response.places() == null
                    || response.places().isEmpty()) {

                return Optional.empty();
            }

            String city =
                    response.places().getFirst().placeName();

            return Optional.of(
                    new PostalCodeLookupResponse(
                            postalCode,
                            city,
                            countryCode
                    )
            );

        } catch (HttpClientErrorException.NotFound ex) {

            return Optional.empty();

        }
    }

    @Override
    public List<PostalCodeLookupResponse> suggest(
            CountryCode countryCode,
            String query
    ) {

        return lookup(countryCode, query)
                .map(List::of)
                .orElse(List.of());
    }
}