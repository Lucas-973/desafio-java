package com.desafio_java.mock_membros.dto;

import com.desafio_java.mock_membros.model.Atribuicao;

public record MembroResponseDto(Long id, String nome, Atribuicao atribuicao) {
}
