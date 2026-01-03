package com.kcserver.util;

public final class StringUtils {

    private StringUtils() {
        // Utility class
    }

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}