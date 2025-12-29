package com.kcserver.persistence.converter;

import com.kcserver.enumtype.Sex;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class SexConverter extends AbstractCodeEnumConverter<Sex> {

    public SexConverter() {
        super(Sex.class);
    }
}