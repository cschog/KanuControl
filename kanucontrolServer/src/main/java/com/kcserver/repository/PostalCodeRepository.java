package com.kcserver.repository;

import com.kcserver.entity.PostalCode;
import com.kcserver.enumtype.CountryCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository

public interface PostalCodeRepository
        extends JpaRepository<PostalCode, Long> {

    Optional<PostalCode> findFirstByCountryCodeAndPostalCode(
            CountryCode countryCode,
            String postalCode
    );

    List<PostalCode> findTop200ByCountryCodeAndPostalCodeStartingWithOrderByPostalCode(
            CountryCode countryCode,
            String postalCode
    );

    List<PostalCode> findTop20ByCountryCodeAndCityStartingWithIgnoreCaseOrderByCity(
            CountryCode countryCode,
            String city

    );

    List<PostalCode> findTop200ByCountryCodeAndCityContainingIgnoreCaseOrderByCity(
            CountryCode countryCode,
            String city
    );

    @Modifying
    @Transactional
    long deleteByCountryCode(CountryCode countryCode);


    long countByCountryCode(CountryCode countryCode);

    Optional<PostalCode> findTopByCountryCodeOrderByImportedAtDesc(

            CountryCode countryCode

    );

}
