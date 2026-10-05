package com.kcserver.core.converter;

import com.kcserver.kjfp.enumtype.CountryCode;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class CountryCodeConverter
        extends AbstractCodeEnumConverter<CountryCode> {

    public CountryCodeConverter() {
        super(CountryCode.class);
    }
}