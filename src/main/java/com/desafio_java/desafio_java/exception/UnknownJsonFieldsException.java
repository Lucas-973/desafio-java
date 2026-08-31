package com.desafio_java.desafio_java.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class UnknownJsonFieldsException extends RuntimeException {

    private final transient List<String> fields;

    public UnknownJsonFieldsException(List<String> fields) {
        super("A requisição contém campos não reconhecidos");
        this.fields = List.copyOf(fields);
    }
}
