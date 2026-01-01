package com.kcserver.persistence.converter;

import com.kcserver.enumtype.CountryCode;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class CountryCodeConverter
        extends AbstractCodeEnumConverter<CountryCode> {

    public CountryCodeConverter() {
        super(CountryCode.class);
    }
}