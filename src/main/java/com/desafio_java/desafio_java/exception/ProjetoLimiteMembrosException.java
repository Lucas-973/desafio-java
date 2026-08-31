package com.desafio_java.desafio_java.exception;


import lombok.Getter;

import java.util.Set;

@Getter
public class ProjetoLimiteMembrosException extends RuntimeException {

    private final Set<Long> membrosIds;

    public ProjetoLimiteMembrosException(Set<Long> membrosIds) {
        super("Um ou mais membros atingiram o limite de projetos ativos");
        this.membrosIds = Set.copyOf(membrosIds);
    }

}
