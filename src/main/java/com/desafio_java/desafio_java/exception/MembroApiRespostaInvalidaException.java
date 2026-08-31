package com.desafio_java.desafio_java.exception;

public class MembroApiRespostaInvalidaException extends RuntimeException {

    public MembroApiRespostaInvalidaException(Throwable cause) {
        super("API externa rejeitou a requisição", cause);
    }
}
