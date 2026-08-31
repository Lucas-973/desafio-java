package com.desafio_java.mock_membros.service;

import com.desafio_java.mock_membros.dto.MembroCreateDto;
import com.desafio_java.mock_membros.dto.MembroResponseDto;
import com.desafio_java.mock_membros.model.Atribuicao;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MembroServiceTest {

    private final MembroService service = new MembroService();

    @Test
    void deveCriarConsultarEListarMembro() {
        var criado = service.criar(new MembroCreateDto("Ana", Atribuicao.FUNCIONARIO));

        assertEquals(7L, criado.id());
        assertEquals(criado, service.buscarPorId(criado.id()).orElseThrow());
        assertEquals(7, service.listar().size());
    }

    @Test
    void deveRetornarVazioParaMembroInexistente() {
        assertTrue(service.buscarPorId(999L).isEmpty());
    }

    @Test
    void deveIniciarComDoisGerentesEQuatroFuncionarios() {
        var membros = service.listar();

        assertEquals(6, membros.size());
        assertEquals(2, membros.stream()
                .filter(membro -> membro.atribuicao() == Atribuicao.GERENTE)
                .count());
        assertEquals(4, membros.stream()
                .filter(membro -> membro.atribuicao() == Atribuicao.FUNCIONARIO)
                .count());
    }

    @Test
    void deveBuscarSomenteIdsInformadosEIgnorarInexistentes() {
        var membros = service.buscarPorIds(Set.of(1L, 3L, 999L));

        assertEquals(List.of(1L, 3L),
                membros.stream().map(MembroResponseDto::id).toList());
    }
}
