package com.kcserver.config;

import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {

            // LocalDate / LocalDateTime
            builder.modules(new JavaTimeModule());

            // "" → null für Enums (CountryCode!)
            builder.postConfigurer(mapper ->
                    mapper.coercionConfigFor(LogicalType.Enum)
                            .setCoercion(
                                    CoercionInputShape.EmptyString,
                                    CoercionAction.AsNull
                            )
            );

            // "" → null für LocalDate
            builder.postConfigurer(mapper ->
                    mapper.coercionConfigFor(LogicalType.DateTime)
                            .setCoercion(
                                    CoercionInputShape.EmptyString,
                                    CoercionAction.AsNull
                            )
            );
        };
    }
}