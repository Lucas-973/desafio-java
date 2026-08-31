package com.desafio_java.desafio_java.service;

import com.desafio_java.desafio_java.entity.ProjetoStatus;
import com.desafio_java.desafio_java.exception.ProjetoStatusTransitionException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProjetoStatusTransitionPolicy {

    private static final Map<ProjetoStatus, ProjetoStatus> PROXIMO_STATUS = Map.of(
            ProjetoStatus.EM_ANALISE, ProjetoStatus.ANALISE_REALIZADA,
            ProjetoStatus.ANALISE_REALIZADA, ProjetoStatus.ANALISE_APROVADA,
            ProjetoStatus.ANALISE_APROVADA, ProjetoStatus.PLANEJADO,
            ProjetoStatus.PLANEJADO, ProjetoStatus.INICIADO,
            ProjetoStatus.INICIADO, ProjetoStatus.EM_ANDAMENTO,
            ProjetoStatus.EM_ANDAMENTO, ProjetoStatus.ENCERRADO
    );

    public void validar(ProjetoStatus atual, ProjetoStatus novo) {
        if (atual == novo) {
            return;
        }

        if (PROXIMO_STATUS.get(atual) != novo) {
            throw new ProjetoStatusTransitionException(atual, novo);
        }
    }
}
