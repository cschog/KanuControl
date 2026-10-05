package com.kcserver.kjfp.repository.postalcode;

import java.util.Optional;

public interface PostalCode {

    Optional<PostalCode> findFirstByCountryCodeAndPostalCode(
            String countryCode,
            String postalCode
    );
}
