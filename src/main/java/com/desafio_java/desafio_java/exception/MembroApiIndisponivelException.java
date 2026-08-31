package com.desafio_java.desafio_java.exception;

public class MembroApiIndisponivelException extends RuntimeException {

    public MembroApiIndisponivelException(Throwable cause) {
        super("API externa indisponível", cause);
    }
}
