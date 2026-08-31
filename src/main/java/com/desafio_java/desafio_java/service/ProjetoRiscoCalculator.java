package com.desafio_java.desafio_java.service;

import com.desafio_java.desafio_java.entity.ClassificacaoRisco;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class ProjetoRiscoCalculator {

    private static final BigDecimal LIMITE_BAIXO = new BigDecimal("100000");
    private static final BigDecimal LIMITE_MEDIO = new BigDecimal("500000");

    public ClassificacaoRisco calcular(
            BigDecimal orcamento,
            LocalDate dataInicio,
            LocalDate dataFimPrevisao
    ) {
        if (orcamento.compareTo(LIMITE_MEDIO) > 0
                || dataFimPrevisao.isAfter(dataInicio.plusMonths(6))) {
            return ClassificacaoRisco.ALTO;
        }

        if (orcamento.compareTo(LIMITE_BAIXO) > 0
                || dataFimPrevisao.isAfter(dataInicio.plusMonths(3))) {
            return ClassificacaoRisco.MEDIO;
        }

        return ClassificacaoRisco.BAIXO;
    }
}
