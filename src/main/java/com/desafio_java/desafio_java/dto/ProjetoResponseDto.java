package com.desafio_java.desafio_java.dto;

import com.desafio_java.desafio_java.client.dto.MembroResponseDto;
import com.desafio_java.desafio_java.entity.ClassificacaoRisco;
import com.desafio_java.desafio_java.entity.ProjetoStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record ProjetoResponseDto(
        Long id,
        String nome,
        LocalDate dataInicio,
        LocalDate dataFimPrevisao,
        LocalDate dataFimFinal,
        BigDecimal orcamento,
        String descricao,
        MembroResponseDto gerente,
        ProjetoStatus situacao,
        ClassificacaoRisco classificacaoRisco,
        Set<MembroResponseDto> membros
) {
}
