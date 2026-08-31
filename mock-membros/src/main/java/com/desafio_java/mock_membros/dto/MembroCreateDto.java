package com.desafio_java.mock_membros.dto;

import com.desafio_java.mock_membros.model.Atribuicao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MembroCreateDto(
        @NotBlank @Size(max = 150) String nome,
        @NotNull Atribuicao atribuicao
) {
}
