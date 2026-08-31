package com.desafio_java.desafio_java.dto;

import com.desafio_java.desafio_java.entity.ProjetoStatus;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record ProjetoUpdateDto(

        @Pattern(regexp = "(?s).*\\S.*", message = "Nome deve ser informado")
        @Size(max = 150, message = "Nome deve ter entre 1 e 150 caracteres")
        String nome,

        @FutureOrPresent(message = "Data de início deve ser igual ou posterior a data atual")
        LocalDate dataInicio,

        LocalDate dataFimPrevisao,

        @Positive(message = "Orçamento deve ser maior que zero")
        @Digits(integer = 17, fraction = 2, message = "Orçamento deve ter até 17 dígitos inteiros e 2 decimais")
        BigDecimal orcamento,

        @Pattern(regexp = "(?s).*\\S.*", message = "Descrição deve ser informada")
        @Size(max = 2000, message = "Descrição deve ter entre 1 e 2000 caracteres")
        String descricao,

        @Positive(message = "Identificador do gerente deve ser maior que zero")
        Long gerenteId,

        ProjetoStatus situacao,

        @Size(
                min = 1,
                max = 10,
                message = "Projeto deve ter entre 1 e 10 membros"
        )
        Set<@NotNull(message = "Identificador do membro não pode ser nulo")
                @Positive(message = "Identificador do membro deve ser maior que zero") Long> membrosIds
) {
}
