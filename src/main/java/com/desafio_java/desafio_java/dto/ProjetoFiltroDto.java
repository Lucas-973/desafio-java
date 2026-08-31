package com.desafio_java.desafio_java.dto;

import com.desafio_java.desafio_java.entity.ProjetoStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjetoFiltroDto(
        String nome,
        ProjetoStatus situacao,
        @Positive(message = "Identificador do gerente deve ser maior que zero")
        Long gerenteId,
        LocalDate dataInicioDe,
        LocalDate dataInicioAte,
        @PositiveOrZero(message = "Orçamento mínimo não pode ser negativo")
        BigDecimal orcamentoMinimo,
        @PositiveOrZero(message = "Orçamento máximo não pode ser negativo")
        BigDecimal orcamentoMaximo
) {

    @AssertTrue(message = "Data inicial do filtro deve ser anterior ou igual à data final")
    @JsonIgnore
    @Schema(hidden = true)
    public boolean isPeriodoValido() {
        return dataInicioDe == null
                || dataInicioAte == null
                || !dataInicioDe.isAfter(dataInicioAte);
    }

    @AssertTrue(message = "Orçamento mínimo deve ser menor ou igual ao orçamento máximo")
    @JsonIgnore
    @Schema(hidden = true)
    public boolean isIntervaloOrcamentoValido() {
        return orcamentoMinimo == null
                || orcamentoMaximo == null
                || orcamentoMinimo.compareTo(orcamentoMaximo) <= 0;
    }
}
