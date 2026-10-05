package com.kcserver.kjfp.repository.postalcode;

import com.kcserver.kjfp.entity.postalcode.PostalCodeCountry;
import com.kcserver.kjfp.enumtype.CountryCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostalCodeCountryRepository
        extends JpaRepository<PostalCodeCountry, CountryCode> {

    List<PostalCodeCountry> findByEnabledTrueAndAutoImportTrue();
}