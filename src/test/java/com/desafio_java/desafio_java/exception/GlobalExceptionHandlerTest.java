package com.desafio_java.desafio_java.exception;

import com.desafio_java.desafio_java.dto.ExceptionResponseDto;
import com.desafio_java.desafio_java.entity.ProjetoStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest request = request();

    @Test
    void deveTratarTransicaoDeStatusComoConflito() {
        ResponseEntity<ExceptionResponseDto> response = handler.handleProjetoStatusTransition(
                new ProjetoStatusTransitionException(
                        ProjetoStatus.EM_ANALISE,
                        ProjetoStatus.ENCERRADO
                ),
                request
        );

        ExceptionResponseDto body = assertResponse(response, HttpStatus.CONFLICT);
        assertEquals("situacao", body.violations().getFirst().field());
    }

    @Test
    void deveTratarViolacaoDeIntegridadeSemExporDetalhes() {
        ResponseEntity<ExceptionResponseDto> response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("segredo do banco"),
                request
        );

        ExceptionResponseDto body = assertResponse(response, HttpStatus.CONFLICT);
        assertEquals(
                "A operação viola uma restrição de integridade dos dados",
                body.message()
        );
    }

    @Test
    void deveTratarErroInesperadoSemExporDetalhes() {
        ResponseEntity<ExceptionResponseDto> response = handler.handleUnexpectedException(
                new RuntimeException("detalhe interno"),
                request
        );

        ExceptionResponseDto body = assertResponse(response, HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals("Ocorreu um erro interno inesperado", body.message());
    }

    @Test
    void deveTratarRequisicaoRejeitadaPelaApiDeMembrosComoBadGateway() {
        ResponseEntity<ExceptionResponseDto> response = handler.handleMembroApiRespostaInvalida(
                new MembroApiRespostaInvalidaException(new RuntimeException()),
                request
        );

        ExceptionResponseDto body = assertResponse(response, HttpStatus.BAD_GATEWAY);
        assertEquals("API externa de membros rejeitou a requisição", body.message());
    }

    private ExceptionResponseDto assertResponse(
            ResponseEntity<ExceptionResponseDto> response,
            HttpStatus status
    ) {
        assertEquals(status, response.getStatusCode());
        ExceptionResponseDto body = response.getBody();
        assertNotNull(body);
        assertEquals(status.value(), body.status());
        assertEquals("/teste", body.path());
        return body;
    }

    private HttpServletRequest request() {
        HttpServletRequest mockedRequest = mock(HttpServletRequest.class);
        when(mockedRequest.getRequestURI()).thenReturn("/teste");
        when(mockedRequest.getMethod()).thenReturn("POST");
        return mockedRequest;
    }
}
