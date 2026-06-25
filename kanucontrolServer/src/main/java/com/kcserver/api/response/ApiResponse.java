package com.kcserver.api.response;

import java.util.List;

public record ApiResponse<T>(
        T data,
        List<String> warnings
) {
    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data, List.of());
    }

    public static <T> ApiResponse<T> of(
            T data,
            List<String> warnings
    ) {
        return new ApiResponse<>(data, warnings);
    }
}
