package com.desafio_java.desafio_java.service;

import com.desafio_java.desafio_java.entity.ClassificacaoRisco;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjetoRiscoCalculatorTest {

    private final ProjetoRiscoCalculator calculator = new ProjetoRiscoCalculator();
    private final LocalDate inicio = LocalDate.of(2026, 1, 1);

    @Test
    void deveClassificarFaixasDeOrcamentoNosLimites() {
        assertEquals(ClassificacaoRisco.BAIXO, calcular("100000", inicio.plusMonths(3)));
        assertEquals(ClassificacaoRisco.MEDIO, calcular("100000.01", inicio.plusMonths(3)));
        assertEquals(ClassificacaoRisco.MEDIO, calcular("500000", inicio.plusMonths(3)));
        assertEquals(ClassificacaoRisco.ALTO, calcular("500000.01", inicio.plusMonths(3)));
    }

    @Test
    void deveClassificarFaixasDeDuracaoNosLimites() {
        assertEquals(ClassificacaoRisco.BAIXO, calcular("1", inicio.plusMonths(3)));
        assertEquals(ClassificacaoRisco.MEDIO, calcular("1", inicio.plusMonths(3).plusDays(1)));
        assertEquals(ClassificacaoRisco.MEDIO, calcular("1", inicio.plusMonths(6)));
        assertEquals(ClassificacaoRisco.ALTO, calcular("1", inicio.plusMonths(6).plusDays(1)));
    }

    private ClassificacaoRisco calcular(String orcamento, LocalDate fim) {
        return calculator.calcular(new BigDecimal(orcamento), inicio, fim);
    }
}
