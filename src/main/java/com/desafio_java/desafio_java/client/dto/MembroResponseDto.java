package com.desafio_java.desafio_java.client.dto;

public record MembroResponseDto(
        Long id,
        String nome,
        AtribuicaoMembro atribuicao
) {
}
