package com.desafio_java.desafio_java.exception;

import com.desafio_java.desafio_java.entity.ProjetoStatus;
import lombok.Getter;

@Getter
public class ProjetoStatusTransitionException extends RuntimeException {

    private final ProjetoStatus statusAtual;
    private final ProjetoStatus statusSolicitado;

    public ProjetoStatusTransitionException(
            ProjetoStatus statusAtual,
            ProjetoStatus statusSolicitado
    ) {
        super("Transição de situação do projeto não permitida");
        this.statusAtual = statusAtual;
        this.statusSolicitado = statusSolicitado;
    }
}
