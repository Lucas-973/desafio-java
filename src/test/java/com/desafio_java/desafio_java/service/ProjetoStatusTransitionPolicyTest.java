package com.desafio_java.desafio_java.service;

import com.desafio_java.desafio_java.entity.ProjetoStatus;
import com.desafio_java.desafio_java.exception.ProjetoStatusTransitionException;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjetoStatusTransitionPolicyTest {

    private static final Map<ProjetoStatus, ProjetoStatus> PROXIMOS = Map.of(
            ProjetoStatus.EM_ANALISE, ProjetoStatus.ANALISE_REALIZADA,
            ProjetoStatus.ANALISE_REALIZADA, ProjetoStatus.ANALISE_APROVADA,
            ProjetoStatus.ANALISE_APROVADA, ProjetoStatus.PLANEJADO,
            ProjetoStatus.PLANEJADO, ProjetoStatus.INICIADO,
            ProjetoStatus.INICIADO, ProjetoStatus.EM_ANDAMENTO,
            ProjetoStatus.EM_ANDAMENTO, ProjetoStatus.ENCERRADO
    );

    private final ProjetoStatusTransitionPolicy policy = new ProjetoStatusTransitionPolicy();

    @TestFactory
    Stream<DynamicTest> deveAceitarTodasAsTransicoesValidas() {
        Stream<DynamicTest> mesmasSituacoes = Stream.of(ProjetoStatus.values())
                .map(status -> DynamicTest.dynamicTest(
                        status + " -> " + status,
                        () -> assertDoesNotThrow(() -> policy.validar(status, status))
                ));
        Stream<DynamicTest> proximasSituacoes = PROXIMOS.entrySet().stream()
                .map(entry -> DynamicTest.dynamicTest(
                        entry.getKey() + " -> " + entry.getValue(),
                        () -> assertDoesNotThrow(
                                () -> policy.validar(entry.getKey(), entry.getValue())
                        )
                ));
        return Stream.concat(mesmasSituacoes, proximasSituacoes);
    }

    @TestFactory
    Stream<DynamicTest> deveRejeitarTodasAsTransicoesInvalidas() {
        Set<String> validas = PROXIMOS.entrySet().stream()
                .map(entry -> chave(entry.getKey(), entry.getValue()))
                .collect(java.util.stream.Collectors.toSet());

        return Stream.of(ProjetoStatus.values())
                .flatMap(atual -> Stream.of(ProjetoStatus.values())
                        .filter(novo -> atual != novo)
                        .filter(novo -> !validas.contains(chave(atual, novo)))
                        .map(novo -> DynamicTest.dynamicTest(
                                atual + " -> " + novo,
                                () -> assertThrows(
                                        ProjetoStatusTransitionException.class,
                                        () -> policy.validar(atual, novo)
                                )
                        )));
    }

    private static String chave(ProjetoStatus atual, ProjetoStatus novo) {
        return atual + ":" + novo;
    }
}
