package com.kcserver.csv.converter;

import java.util.HashMap;
import java.util.Map;

public final class CsvConverterRegistry {

    private static final Map<String, CsvValueConverter> CONVERTERS = new HashMap<>();

    static {
        register("sex_de", new SexDeConverter());
        register("date_de", new DateDeConverter());
    }

    private CsvConverterRegistry() {
    }

    public static void register(String name, CsvValueConverter converter) {
        CONVERTERS.put(name, converter);
    }

    public static CsvValueConverter get(String name) {
        CsvValueConverter c = CONVERTERS.get(name);
        if (c == null) {
            throw new IllegalArgumentException(
                    "Unbekannter CSV-Converter: " + name
            );
        }
        return c;
    }
}