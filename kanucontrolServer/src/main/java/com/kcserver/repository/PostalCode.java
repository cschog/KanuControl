package com.kcserver.repository;

import java.util.Optional;

public interface PostalCode {

    Optional<PostalCode> findFirstByCountryCodeAndPostalCode(
            String countryCode,
            String postalCode
    );
}
