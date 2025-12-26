package com.kcserver.exception;

import java.util.Map;

public record ApiError(
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors
) {
    public static ApiError simple(int status, String error, String message) {
        return new ApiError(status, error, message, null);
    }
}