package com.desafio_java.desafio_java.service;

import com.desafio_java.desafio_java.client.MembroClient;
import com.desafio_java.desafio_java.client.dto.AtribuicaoMembro;
import com.desafio_java.desafio_java.client.dto.MembroResponseDto;
import com.desafio_java.desafio_java.dto.*;
import com.desafio_java.desafio_java.entity.Projeto;
import com.desafio_java.desafio_java.entity.ProjetoStatus;
import com.desafio_java.desafio_java.exception.*;
import com.desafio_java.desafio_java.mapper.ProjetoMapper;
import com.desafio_java.desafio_java.repository.MembroAlocacaoLockRepository;
import com.desafio_java.desafio_java.repository.ProjetoRepository;
import com.desafio_java.desafio_java.repository.projection.MembroProjetosAtivosProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    @Mock ProjetoRepository projetoRepository;
    @Mock MembroClient membroClient;
    @Mock ProjetoMapper projetoMapper;
    @Mock ProjetoStatusTransitionPolicy statusTransitionPolicy;
    @Mock MembroAlocacaoLockRepository membroAlocacaoLockRepository;
    private ProjetoService service;

    @BeforeEach
    void setUp() {
        service = new ProjetoService(projetoRepository, membroClient, projetoMapper,
                statusTransitionPolicy, membroAlocacaoLockRepository);
    }

    @Test
    void deveCriarProjetoComMembrosDaApiExterna() {
        ProjetoCreateDto dto = criacao();
        Projeto projeto = projeto(ProjetoStatus.EM_ANALISE);
        ProjetoResponseDto resposta = mock(ProjetoResponseDto.class);
        mockMembrosValidos();
        when(projetoMapper.toEntity(dto)).thenReturn(projeto);
        when(projetoRepository.save(projeto)).thenReturn(projeto);
        when(projetoMapper.toResponseDto(eq(projeto), any(), anySet())).thenReturn(resposta);

        assertSame(resposta, service.criar(dto));
        verify(projetoRepository).save(projeto);
    }

    @Test
    void deveRejeitarMembroNaoFuncionario() {
        ProjetoCreateDto dto = criacao();
        when(membroClient.buscarPorId(1L)).thenReturn(Optional.of(membro(1L, AtribuicaoMembro.GERENTE)));
        when(membroClient.buscarPorId(2L)).thenReturn(Optional.of(membro(2L, AtribuicaoMembro.GERENTE)));

        ProjetoMembrosInvalidosException exception = assertThrows(
                ProjetoMembrosInvalidosException.class, () -> service.criar(dto));

        assertEquals(400, exception.getStatus().value());
        verify(projetoRepository, never()).save(any());
    }

    @Test
    void deveRetornarNotFoundQuandoMembroNaoExiste() {
        ProjetoCreateDto dto = criacao();
        when(membroClient.buscarPorId(1L)).thenReturn(Optional.of(membro(1L, AtribuicaoMembro.GERENTE)));
        when(membroClient.buscarPorId(2L)).thenReturn(Optional.empty());

        ProjetoMembrosInvalidosException exception = assertThrows(
                ProjetoMembrosInvalidosException.class, () -> service.criar(dto));
        assertEquals(404, exception.getStatus().value());
    }

    @Test
    void deveRejeitarQuartoProjetoAtivo() {
        ProjetoCreateDto dto = criacao();
        MembroProjetosAtivosProjection projection = mock(MembroProjetosAtivosProjection.class);
        when(projection.getMembroId()).thenReturn(2L);
        when(projection.getQuantidadeProjetos()).thenReturn(3L);
        mockMembrosValidos();
        when(projetoRepository.contarProjetosAtivosPorMembro(anySet(), anySet()))
                .thenReturn(List.of(projection));

        assertThrows(ProjetoLimiteMembrosException.class, () -> service.criar(dto));

        InOrder ordem = inOrder(membroAlocacaoLockRepository, projetoRepository);
        ordem.verify(membroAlocacaoLockRepository).bloquearEmOrdem(Set.of(2L));
        ordem.verify(projetoRepository).contarProjetosAtivosPorMembro(anySet(), anySet());
    }

    @Test
    void deveAtualizarEquipeEStatus() {
        Projeto projeto = projeto(ProjetoStatus.EM_ANALISE);
        ProjetoUpdateDto dto = atualizacaoParaAnaliseRealizada();
        ProjetoResponseDto resposta = mock(ProjetoResponseDto.class);
        mockMembrosValidos();
        when(projetoRepository.findById(10L)).thenReturn(Optional.of(projeto));
        when(projetoMapper.toResponseDto(eq(projeto), any(), anySet()))
                .thenReturn(resposta);

        service.atualizar(10L, dto);

        verify(statusTransitionPolicy).validar(ProjetoStatus.EM_ANALISE, ProjetoStatus.ANALISE_REALIZADA);
        assertEquals(ProjetoStatus.ANALISE_REALIZADA, projeto.getSituacao());
    }

    @Test
    void deveRejeitarPeriodoInvalidoNaAtualizacao() {
        Projeto projeto = projeto(ProjetoStatus.EM_ANALISE);
        when(projetoRepository.findById(10L)).thenReturn(Optional.of(projeto));
        ProjetoUpdateDto dto = new ProjetoUpdateDto(null, LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(2), null, null, null, null, null);
        assertThrows(ProjetoPeriodoInvalidoException.class, () -> service.atualizar(10L, dto));
    }

    @Test
    void deveRejeitarDataDeInicioPassadaNaAtualizacao() {
        Projeto projeto = projeto(ProjetoStatus.EM_ANALISE);
        when(projetoRepository.findById(10L)).thenReturn(Optional.of(projeto));
        ProjetoUpdateDto dto = new ProjetoUpdateDto(null, LocalDate.now().minusDays(1),
                null, null, null, null, null, null);

        ProjetoPeriodoInvalidoException exception = assertThrows(
                ProjetoPeriodoInvalidoException.class,
                () -> service.atualizar(10L, dto));

        assertEquals("dataInicio", exception.getField());
        verifyNoInteractions(membroAlocacaoLockRepository);
    }

    @Test
    void deveCancelarProjetoExcluivel() {
        Projeto projeto = projeto(ProjetoStatus.PLANEJADO);
        when(projetoRepository.findById(10L)).thenReturn(Optional.of(projeto));
        service.excluir(10L);
        assertEquals(ProjetoStatus.CANCELADO, projeto.getSituacao());
        verifyNoInteractions(membroAlocacaoLockRepository);
    }

    @Test
    void deveRejeitarExclusaoDeProjetoEmAndamento() {
        Projeto projeto = projeto(ProjetoStatus.EM_ANDAMENTO);
        when(projetoRepository.findById(10L)).thenReturn(Optional.of(projeto));
        assertThrows(ProjetoStatusExclusaoException.class, () -> service.excluir(10L));
    }

    @Test
    void deveListarProjetosComUmaUnicaConsultaEmLoteDeMembros() {
        Projeto primeiroProjeto = projeto(10L, Set.of(2L, 3L));
        Projeto segundoProjeto = projeto(11L, Set.of(2L, 4L));
        ProjetoResponseDto primeiraResposta = mock(ProjetoResponseDto.class);
        ProjetoResponseDto segundaResposta = mock(ProjetoResponseDto.class);
        var pageable = PageRequest.of(0, 10);
        when(projetoRepository.findAll(ArgumentMatchers.<Specification<Projeto>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(primeiroProjeto, segundoProjeto)));
        when(membroClient.buscarPorIds(Set.of(1L, 2L, 3L, 4L))).thenReturn(Map.of(
                1L, membro(1L, AtribuicaoMembro.GERENTE),
                2L, membro(2L, AtribuicaoMembro.FUNCIONARIO),
                3L, membro(3L, AtribuicaoMembro.FUNCIONARIO),
                4L, membro(4L, AtribuicaoMembro.FUNCIONARIO)));
        when(projetoMapper.toResponseDto(eq(primeiroProjeto), any(), anySet()))
                .thenReturn(primeiraResposta);
        when(projetoMapper.toResponseDto(eq(segundoProjeto), any(), anySet()))
                .thenReturn(segundaResposta);

        Page<ProjetoResponseDto> resultado = service.buscarTodos(
                new ProjetoFiltroDto("Projeto", null, null, null, null, null, null), pageable);

        assertEquals(2, resultado.getTotalElements());
        assertEquals(List.of(primeiraResposta, segundaResposta), resultado.getContent());
        verify(membroClient).buscarPorIds(Set.of(1L, 2L, 3L, 4L));
        verify(membroClient, never()).buscarPorId(anyLong());
    }

    private void mockMembrosValidos() {
        when(membroClient.buscarPorId(1L)).thenReturn(Optional.of(membro(1L, AtribuicaoMembro.GERENTE)));
        when(membroClient.buscarPorId(2L)).thenReturn(Optional.of(membro(2L, AtribuicaoMembro.FUNCIONARIO)));
    }

    private MembroResponseDto membro(Long id, AtribuicaoMembro atribuicao) {
        return new MembroResponseDto(id, "Membro " + id, atribuicao);
    }

    private ProjetoCreateDto criacao() {
        return new ProjetoCreateDto("Projeto", LocalDate.now().plusDays(1),
                LocalDate.now().plusMonths(2), BigDecimal.valueOf(50_000), "Descricao", 1L, Set.of(2L));
    }

    private ProjetoUpdateDto atualizacaoParaAnaliseRealizada() {
        return new ProjetoUpdateDto(null, null, null, null, null, 1L,
                ProjetoStatus.ANALISE_REALIZADA, Set.of(2L));
    }

    private Projeto projeto(ProjetoStatus status) {
        Projeto projeto = new Projeto();
        projeto.setId(10L);
        projeto.setNome("Projeto");
        projeto.setDataInicio(LocalDate.now());
        projeto.setDataFimPrevisao(LocalDate.now().plusMonths(2));
        projeto.setOrcamento(BigDecimal.TEN);
        projeto.setDescricao("Descricao");
        projeto.setGerenteId(1L);
        projeto.setMembrosIds(Set.of(2L));
        projeto.setSituacao(status);
        return projeto;
    }

    private Projeto projeto(Long id, Set<Long> membrosIds) {
        Projeto projeto = projeto(ProjetoStatus.EM_ANALISE);
        projeto.setId(id);
        projeto.setGerenteId(1L);
        projeto.setMembrosIds(membrosIds);
        return projeto;
    }
}
