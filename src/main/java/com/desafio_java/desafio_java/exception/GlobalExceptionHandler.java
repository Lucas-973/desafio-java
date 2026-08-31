package com.desafio_java.desafio_java.exception;

import com.desafio_java.desafio_java.dto.ExceptionResponseDto;
import com.desafio_java.desafio_java.dto.ExceptionResponseDto.Violation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponseDto> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<Violation> violations = exception.getBindingResult()
                .getAllErrors()
                .stream()
                .map(this::toValidationError)
                .distinct()
                .toList();

        return badRequest(
                "A requisição contém campos inválidos",
                violations,
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponseDto> handleMalformedJson(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        UnrecognizedPropertyException unrecognizedProperty = findUnrecognizedProperty(exception);

        if (unrecognizedProperty != null) {
            return badRequest(
                    "A requisição contém campos não reconhecidos",
                    List.of(new Violation(
                            unrecognizedProperty.getPropertyName(),
                            "Campo não reconhecido"
                    )),
                    request
            );
        }

        return badRequest(
                "O corpo da requisição contém um JSON inválido",
                List.of(),
                request
        );
    }

    @ExceptionHandler(UnknownJsonFieldsException.class)
    public ResponseEntity<ExceptionResponseDto> handleUnknownJsonFields(
            UnknownJsonFieldsException exception,
            HttpServletRequest request
    ) {
        List<Violation> violations = exception.getFields().stream()
                .map(field -> new Violation(field, "Campo não reconhecido"))
                .toList();

        return badRequest(exception.getMessage(), violations, request);
    }

    @ExceptionHandler(ProjetoMembrosInvalidosException.class)
    public ResponseEntity<ExceptionResponseDto> handleProjetoMembrosInvalidos(
            ProjetoMembrosInvalidosException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                exception.getStatus(),
                exception.getMessage(),
                exception.getViolations(),
                request
        );
    }

    @ExceptionHandler(ProjetoNaoEncontradoException.class)
    public ResponseEntity<ExceptionResponseDto> handleProjetoNaoEncontrado(
            ProjetoNaoEncontradoException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                List.of(),
                request
        );
    }

    @ExceptionHandler(ProjetoStatusTransitionException.class)
    public ResponseEntity<ExceptionResponseDto> handleProjetoStatusTransition(
            ProjetoStatusTransitionException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                List.of(new Violation(
                        "situacao",
                        "Transição de " + exception.getStatusAtual()
                                + " para " + exception.getStatusSolicitado()
                                + " não é permitida"
                )),
                request
        );
    }

    @ExceptionHandler(ProjetoPeriodoInvalidoException.class)
    public ResponseEntity<ExceptionResponseDto> handleProjetoPeriodoInvalido(
            ProjetoPeriodoInvalidoException exception,
            HttpServletRequest request
    ) {
        return badRequest(
                "A requisição contém campos inválidos",
                List.of(new Violation(exception.getField(), exception.getMessage())),
                request
        );
    }

    @ExceptionHandler(ProjetoStatusExclusaoException.class)
    public ResponseEntity<ExceptionResponseDto> handleProjetoStatusExclusao(
            ProjetoStatusExclusaoException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                List.of(new Violation(
                        "situacao",
                        "Projeto com situação " + exception.getSituacao()
                                + " não pode ser excluído"
                )),
                request
        );
    }

    @ExceptionHandler(ProjetoLimiteMembrosException.class)
    public ResponseEntity<ExceptionResponseDto> handleProjetoLimiteMembros(
            ProjetoLimiteMembrosException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                List.of(new Violation(
                        "membrosIds",
                        "Membros no limite de 3 projetos ativos: "
                                + exception.getMembrosIds()
                )),
                request
        );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ExceptionResponseDto> handleMethodValidation(
            HandlerMethodValidationException exception,
            HttpServletRequest request
    ) {
        List<Violation> violations = exception.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new Violation(
                                result.getMethodParameter().getParameterName(),
                                error.getDefaultMessage())))
                .toList();
        return badRequest("A requisição contém parâmetros inválidos", violations, request);
    }

    @ExceptionHandler(MembroApiIndisponivelException.class)
    public ResponseEntity<ExceptionResponseDto> handleMembroApiIndisponivel(
            MembroApiIndisponivelException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler(MembroApiRespostaInvalidaException.class)
    public ResponseEntity<ExceptionResponseDto> handleMembroApiRespostaInvalida(
            MembroApiRespostaInvalidaException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_GATEWAY, exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionResponseDto> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Violação de integridade ao processar {} {}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return buildResponse(
                HttpStatus.CONFLICT,
                "A operação viola uma restrição de integridade dos dados",
                List.of(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error(
                "Erro inesperado ao processar {} {}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado",
                List.of(),
                request
        );
    }

    private UnrecognizedPropertyException findUnrecognizedProperty(Throwable throwable) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof UnrecognizedPropertyException unrecognizedProperty) {
                return unrecognizedProperty;
            }
            current = current.getCause();
        }

        return null;
    }

    private Violation toValidationError(ObjectError error) {
        return new Violation(
                error instanceof FieldError fieldError
                        ? fieldError.getField()
                        : error.getObjectName(),
                error.getDefaultMessage()
        );
    }

    private ResponseEntity<ExceptionResponseDto> badRequest(
            String message,
            List<Violation> violations,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, message, violations, request);
    }

    private ResponseEntity<ExceptionResponseDto> buildResponse(
            HttpStatus status,
            String message,
            List<Violation> violations,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status).body(new ExceptionResponseDto(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                violations,
                request.getRequestURI()
        ));
    }
}
