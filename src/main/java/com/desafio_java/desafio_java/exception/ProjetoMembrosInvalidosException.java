package com.desafio_java.desafio_java.exception;

import com.desafio_java.desafio_java.dto.ExceptionResponseDto.Violation;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public class ProjetoMembrosInvalidosException extends RuntimeException {

    private final HttpStatus status;
    private final transient List<Violation> violations;

    public ProjetoMembrosInvalidosException(HttpStatus status, String message, List<Violation> violations) {
        super(message);
        this.status = status;
        this.violations = List.copyOf(violations);
    }
}
