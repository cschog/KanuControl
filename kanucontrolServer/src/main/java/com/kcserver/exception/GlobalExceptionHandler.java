package com.kcserver.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        ex.printStackTrace();

        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(
                new ApiError(
                        HttpStatus.BAD_REQUEST.value(),
                        "VALIDATION_ERROR",
                        "Validation failed",
                        fieldErrors
                )
        );
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex
    ) {
        Map<String, String> fieldErrors = new HashMap<>();

        ex.getConstraintViolations().forEach(v ->
                fieldErrors.put(
                        v.getPropertyPath().toString(),
                        v.getMessage()
                )
        );

        return ResponseEntity.badRequest().body(
                new ApiError(
                        400,
                        "VALIDATION_ERROR",
                        "Validation failed",
                        fieldErrors
                )
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex) {

        return ResponseEntity.status(ex.getStatusCode()).body(
                ApiError.simple(
                        ex.getStatusCode().value(),
                        ex.getStatusCode().toString(),
                        ex.getReason()
                )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {

        return ResponseEntity.badRequest().body(
                ApiError.simple(
                        HttpStatus.BAD_REQUEST.value(),
                        "BAD_REQUEST",
                        ex.getMessage()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {

        log.error("Unhandled exception", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiError.simple(
                        500,
                        ex.getClass().getSimpleName(),
                        ex.getMessage()
                )
        );
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiError> handleBusinessRuleViolation(
            BusinessRuleViolationException ex
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiError.simple(
                        HttpStatus.CONFLICT.value(),
                        "BUSINESS_RULE_VIOLATION",
                        ex.getMessage()
                )
        );
    }
}