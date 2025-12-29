package com.kcserver.persistence.converter;

import com.kcserver.enumtype.CodeEnum;
import jakarta.persistence.AttributeConverter;

public abstract class AbstractCodeEnumConverter<E extends Enum<E> & CodeEnum>
        implements AttributeConverter<E, String> {

    private final Class<E> enumClass;

    protected AbstractCodeEnumConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return attribute != null ? attribute.getCode() : null;
    }

    @Override
    public E convertToEntityAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }

        for (E constant : enumClass.getEnumConstants()) {
            if (constant.getCode().equals(dbValue)) {
                return constant;
            }
        }

        throw new IllegalArgumentException(
                "Unknown code '" + dbValue + "' for enum " + enumClass.getSimpleName()
        );
    }
}