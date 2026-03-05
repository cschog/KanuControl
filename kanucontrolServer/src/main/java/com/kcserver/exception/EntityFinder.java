package com.kcserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

public final class EntityFinder {

    private EntityFinder() {}

    public static <T> T getOr404(Optional<T> optional, String message) {
        return optional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, message));
    }
}