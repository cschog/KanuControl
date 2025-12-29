package com.kcserver.persistence.converter;

import com.kcserver.enumtype.CountryCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CountryCodeConverter
        implements AttributeConverter<CountryCode, String> {

    @Override
    public String convertToDatabaseColumn(CountryCode code) {
        return code == null ? null : code.name();
    }

    @Override
    public CountryCode convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : CountryCode.valueOf(dbValue);
    }
}