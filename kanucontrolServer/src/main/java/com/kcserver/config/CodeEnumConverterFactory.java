package com.kcserver.config;

import com.kcserver.enumtype.CodeEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

@Component
public class CodeEnumConverterFactory implements ConverterFactory<String, CodeEnum> {

    @Override
    public <T extends CodeEnum> Converter<String, T> getConverter(Class<T> targetType) {

        return source -> {
            if (source == null || source.isBlank()) {
                return null;
            }

            for (T enumConstant : targetType.getEnumConstants()) {
                if (enumConstant.getCode().equalsIgnoreCase(source)) {
                    return enumConstant;
                }
            }

            throw new IllegalArgumentException(
                    "Unknown enum code '" + source + "' for enum " + targetType.getSimpleName()
            );
        };
    }
}