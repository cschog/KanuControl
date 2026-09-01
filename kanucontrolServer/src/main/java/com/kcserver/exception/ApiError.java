package com.kcserver.exception;

import java.util.List;
import java.util.Map;

public record ApiError(
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors,
        List<String> missing
) {

    public static ApiError simple(
            int status,
            String error,
            String message
    ) {
        return new ApiError(
                status,
                error,
                message,
                null,
                null
        );
    }

    public static ApiError withMissing(
            int status,
            String error,
            String message,
            List<String> missing
    ) {
        return new ApiError(
                status,
                error,
                message,
                null,
                missing
        );
    }
}