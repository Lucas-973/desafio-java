package com.desafio_java.desafio_java.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjetoDtoValidationTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    @AfterAll
    static void fecharFactory() {
        FACTORY.close();
    }

    @Test
    void deveAceitarMinimoEMaximoDeMembrosNaCriacao() {
        assertTrue(VALIDATOR.validate(criacao(membros(1), "1", 1)).isEmpty());
        assertTrue(VALIDATOR.validate(criacao(membros(10), "1", 1)).isEmpty());
    }

    @Test
    void deveRejeitarQuantidadeDeMembrosForaDosLimitesNaCriacao() {
        assertFalse(VALIDATOR.validate(criacao(Set.of(), "1", 1)).isEmpty());
        assertFalse(VALIDATOR.validate(criacao(membros(11), "1", 1)).isEmpty());
    }

    @Test
    void deveValidarLimitesDeMembrosNaAtualizacao() {
        assertTrue(VALIDATOR.validate(atualizacao(null)).isEmpty());
        assertTrue(VALIDATOR.validate(atualizacao(membros(1))).isEmpty());
        assertTrue(VALIDATOR.validate(atualizacao(membros(10))).isEmpty());
        assertFalse(VALIDATOR.validate(atualizacao(Set.of())).isEmpty());
        assertFalse(VALIDATOR.validate(atualizacao(membros(11))).isEmpty());
    }

    @Test
    void deveRejeitarDatasInvalidasEOrcamentoNaoPositivo() {
        assertFalse(VALIDATOR.validate(criacao(membros(1), "0", -1)).isEmpty());
        assertFalse(VALIDATOR.validate(criacao(membros(1), "-1", 1)).isEmpty());
    }

    @Test
    void deveRejeitarDataDeInicioPassadaNaAtualizacao() {
        ProjetoUpdateDto dto = new ProjetoUpdateDto(
                null, LocalDate.now().minusDays(1), null, null, null, null, null, null
        );

        assertFalse(VALIDATOR.validate(dto).isEmpty());
    }

    @Test
    @SuppressWarnings("DataFlowIssue") // Valores inválidos são necessários para testar Bean Validation.
    void deveRejeitarIdentificadoresNaoPositivos() {
        ProjetoCreateDto criacao = new ProjetoCreateDto(
                "Projeto", LocalDate.now().plusDays(1), LocalDate.now().plusDays(2),
                BigDecimal.ONE, "Descricao", 0L, Set.of(-1L)
        );
        ProjetoUpdateDto atualizacao = new ProjetoUpdateDto(
                null, null, null, null, null, -1L, null, Set.of(0L)
        );

        assertFalse(VALIDATOR.validate(criacao).isEmpty());
        assertFalse(VALIDATOR.validate(atualizacao).isEmpty());
    }

    private ProjetoCreateDto criacao(Set<Long> membros, String orcamento, int diasAteFim) {
        LocalDate inicio = LocalDate.now().plusDays(1);
        return new ProjetoCreateDto(
                "Projeto", inicio, inicio.plusDays(diasAteFim),
                new BigDecimal(orcamento), "Descrição", 100L, membros
        );
    }

    private ProjetoUpdateDto atualizacao(Set<Long> membros) {
        return new ProjetoUpdateDto(
                null, null, null, null, null, null, null, membros
        );
    }

    private Set<Long> membros(int quantidade) {
        return LongStream.rangeClosed(1, quantidade)
                .boxed()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
