package com.desafio_java.desafio_java.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjetoFiltroDtoValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void deveAceitarIntervalosValidos() {
        ProjetoFiltroDto filtro = filtro(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("1000"),
                new BigDecimal("5000")
        );

        assertTrue(validator.validate(filtro).isEmpty());
    }

    @Test
    void deveRejeitarPeriodoInvertido() {
        ProjetoFiltroDto filtro = filtro(
                LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 1, 1),
                null,
                null
        );

        assertEquals(1, validator.validate(filtro).size());
    }

    @Test
    void deveRejeitarIntervaloDeOrcamentoInvertido() {
        ProjetoFiltroDto filtro = filtro(
                null,
                null,
                new BigDecimal("5000"),
                new BigDecimal("1000")
        );

        assertEquals(1, validator.validate(filtro).size());
    }

    @Test
    void deveRejeitarOrcamentosNegativos() {
        ProjetoFiltroDto filtro = filtro(
                null,
                null,
                new BigDecimal("-1"),
                new BigDecimal("-2")
        );

        assertEquals(3, validator.validate(filtro).size());
    }

    private ProjetoFiltroDto filtro(
            LocalDate dataInicioDe,
            LocalDate dataInicioAte,
            BigDecimal orcamentoMinimo,
            BigDecimal orcamentoMaximo
    ) {
        return new ProjetoFiltroDto(
                null,
                null,
                null,
                dataInicioDe,
                dataInicioAte,
                orcamentoMinimo,
                orcamentoMaximo
        );
    }
}
