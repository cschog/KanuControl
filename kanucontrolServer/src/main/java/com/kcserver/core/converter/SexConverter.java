package com.kcserver.core.converter;

import com.kcserver.kjfp.enumtype.Sex;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SexConverter implements AttributeConverter<Sex, String> {

    @Override
    public String convertToDatabaseColumn(Sex sex) {
        return sex == null ? null : sex.getCode();
    }

    @Override
    public Sex convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : Sex.fromCode(dbValue);
    }
}