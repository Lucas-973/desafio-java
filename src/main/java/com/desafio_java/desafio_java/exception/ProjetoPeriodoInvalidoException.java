package com.desafio_java.desafio_java.exception;

import lombok.Getter;

@Getter
public class ProjetoPeriodoInvalidoException extends RuntimeException {

    private final String field;

    public ProjetoPeriodoInvalidoException() {
        this("dataFimPrevisao", "Data final prevista deve ser igual ou posterior a data de início");
    }

    public ProjetoPeriodoInvalidoException(String field, String message) {
        super(message);
        this.field = field;
    }

}
