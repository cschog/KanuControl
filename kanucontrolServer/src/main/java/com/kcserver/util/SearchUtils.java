package com.kcserver.util;

import java.util.Arrays;
import java.util.List;

public class SearchUtils {

    public static List<String> splitSearch(String search) {
        if (search == null || search.isBlank()) {
            return List.of();
        }

        return Arrays.stream(search.toLowerCase().trim().split("\\s+"))
                .filter(s -> !s.isBlank())
                .toList();
    }
}
