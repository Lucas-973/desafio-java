package com.desafio_java.desafio_java.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record ProjetoCreateDto(

        @NotBlank(message = "Nome é obrigatório e deve ser informado")
        @Size(max = 150, message = "Nome deve ter entre 1 e 150 caracteres")
        String nome,

        @NotNull(message = "Data de início é obrigatória e deve ser informada")
        @FutureOrPresent(message = "Data de início deve ser igual ou posterior à data atual")
        LocalDate dataInicio,

        @NotNull(message = "Data de fim prevista é obrigatória e deve ser informada")
        LocalDate dataFimPrevisao,

        @NotNull(message = "Orçamento é obrigatório e deve ser informado")
        @Positive(message = "Orçamento deve ser maior que zero")
        @Digits(integer = 17, fraction = 2, message = "Orçamento deve ter até 17 dígitos inteiros e 2 decimais")
        BigDecimal orcamento,

        @NotBlank(message = "Descrição é obrigatória e deve ser informada")
        @Size(max = 2000, message = "Descrição deve ter entre 1 e 2000 caracteres")
        String descricao,

        @NotNull(message = "Gerente é obrigatório e deve ser informado")
        @Positive(message = "Identificador do gerente deve ser maior que zero")
        Long gerenteId,

        @NotEmpty(message = "Projeto deve ter ao menos um membro")
        @Size(max = 10, message = "Projeto deve ter no máximo 10 membros")
        Set<@NotNull(message = "Identificador do membro não pode ser nulo")
                @Positive(message = "Identificador do membro deve ser maior que zero") Long> membrosIds
) {

    @AssertTrue(message = "Data de fim prevista deve ser igual ou posterior à data de início")
    @JsonIgnore
    @Schema(hidden = true)
    public boolean isPeriodoValido() {
        return dataInicio == null
                || dataFimPrevisao == null
                || !dataFimPrevisao.isBefore(dataInicio);
    }
}
