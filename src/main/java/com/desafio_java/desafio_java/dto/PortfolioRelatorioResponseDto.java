package com.desafio_java.desafio_java.dto;

import com.desafio_java.desafio_java.entity.ProjetoStatus;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioRelatorioResponseDto(
        List<ProjetosPorStatusDto> projetosPorStatus,
        BigDecimal mediaDuracaoProjetosEncerradosDias,
        Long totalMembrosUnicosAlocados
) {
    public record ProjetosPorStatusDto(
            ProjetoStatus status,
            Long quantidadeProjetos,
            BigDecimal totalOrcado
    ) {
    }
}
