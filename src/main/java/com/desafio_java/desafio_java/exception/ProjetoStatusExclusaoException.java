package com.desafio_java.desafio_java.exception;

import com.desafio_java.desafio_java.entity.ProjetoStatus;
import lombok.Getter;

@Getter
public class ProjetoStatusExclusaoException extends RuntimeException {

    private final ProjetoStatus situacao;

    public ProjetoStatusExclusaoException(ProjetoStatus situacao) {
        super("Projeto não pode ser excluído na situação " + situacao);
        this.situacao = situacao;
    }

}
