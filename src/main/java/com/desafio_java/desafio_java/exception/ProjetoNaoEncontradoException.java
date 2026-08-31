package com.desafio_java.desafio_java.exception;

public class ProjetoNaoEncontradoException extends RuntimeException {

    public ProjetoNaoEncontradoException() {
        super("Projeto não encontrado");
    }
}
