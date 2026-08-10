package com.kcserver.repository.postalcode;

import java.util.Optional;

public interface PostalCode {

    Optional<PostalCode> findFirstByCountryCodeAndPostalCode(
            String countryCode,
            String postalCode
    );
}
