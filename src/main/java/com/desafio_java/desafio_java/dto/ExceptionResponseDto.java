package com.desafio_java.desafio_java.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

public record ExceptionResponseDto(
        Instant timestamp,
        int status,
        String error,
        String message,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<Violation> violations,
        String path
) {
    public record Violation(
            String field,
            String message
    ) {
    }
}
