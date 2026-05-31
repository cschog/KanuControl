package com.kcserver.repository;

import com.kcserver.entity.PostalCodeCountry;
import com.kcserver.enumtype.CountryCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostalCodeCountryRepository
        extends JpaRepository<PostalCodeCountry, CountryCode> {

    List<PostalCodeCountry> findByEnabledTrueAndAutoImportTrue();
}