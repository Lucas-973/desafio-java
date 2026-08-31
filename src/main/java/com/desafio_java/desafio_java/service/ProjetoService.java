package com.desafio_java.desafio_java.service;

import com.desafio_java.desafio_java.client.MembroClient;
import com.desafio_java.desafio_java.client.dto.AtribuicaoMembro;
import com.desafio_java.desafio_java.client.dto.MembroResponseDto;
import com.desafio_java.desafio_java.dto.ExceptionResponseDto.Violation;
import com.desafio_java.desafio_java.dto.ProjetoCreateDto;
import com.desafio_java.desafio_java.dto.ProjetoFiltroDto;
import com.desafio_java.desafio_java.dto.ProjetoResponseDto;
import com.desafio_java.desafio_java.dto.ProjetoUpdateDto;
import com.desafio_java.desafio_java.entity.Projeto;
import com.desafio_java.desafio_java.entity.ProjetoStatus;
import com.desafio_java.desafio_java.exception.*;
import com.desafio_java.desafio_java.mapper.ProjetoMapper;
import com.desafio_java.desafio_java.repository.MembroAlocacaoLockRepository;
import com.desafio_java.desafio_java.repository.ProjetoRepository;
import com.desafio_java.desafio_java.repository.projection.MembroProjetosAtivosProjection;
import com.desafio_java.desafio_java.repository.specification.ProjetoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjetoService {

    private static final int LIMITE_PROJETOS_ATIVOS_POR_MEMBRO = 3;
    private static final Set<ProjetoStatus> SITUACOES_INATIVAS = Set.of(
            ProjetoStatus.ENCERRADO, ProjetoStatus.CANCELADO);
    private static final Set<ProjetoStatus> SITUACOES_NAO_EXCLUIVEIS = Set.of(
            ProjetoStatus.INICIADO, ProjetoStatus.EM_ANDAMENTO, ProjetoStatus.ENCERRADO);

    private final ProjetoRepository projetoRepository;
    private final MembroClient membroClient;
    private final ProjetoMapper projetoMapper;
    private final ProjetoStatusTransitionPolicy statusTransitionPolicy;
    private final MembroAlocacaoLockRepository membroAlocacaoLockRepository;

    @Transactional
    public ProjetoResponseDto criar(ProjetoCreateDto dto) {
        Map<Long, MembroResponseDto> membros = buscarMembros(dto.gerenteId(), dto.membrosIds());
        validarMembrosRelacionados(dto.gerenteId(), dto.membrosIds(), membros);
        validarLimiteProjetosAtivos(dto.membrosIds(), null, ProjetoStatus.EM_ANALISE);
        Projeto projeto = projetoRepository.save(projetoMapper.toEntity(dto));
        return montarResposta(projeto, membros);
    }

    @Transactional(readOnly = true)
    public ProjetoResponseDto buscarPorId(Long id) {
        return montarResposta(buscarProjeto(id));
    }

    @Transactional(readOnly = true)
    public Page<ProjetoResponseDto> buscarTodos(ProjetoFiltroDto filtro, Pageable pageable) {
        Page<Projeto> projetos = projetoRepository.findAll(
                ProjetoSpecification.comFiltros(filtro), pageable);
        Set<Long> membrosIds = projetos.getContent().stream()
                .flatMap(projeto -> idsDosMembros(projeto).stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, MembroResponseDto> membros = membroClient.buscarPorIds(membrosIds);
        return projetos.map(projeto -> {
            validarMembrosRelacionados(projeto.getGerenteId(), projeto.getMembrosIds(), membros);
            return montarResposta(projeto, membros);
        });
    }

    private Set<Long> idsDosMembros(Projeto projeto) {
        Set<Long> ids = new LinkedHashSet<>(projeto.getMembrosIds());
        ids.add(projeto.getGerenteId());
        return ids;
    }

    @Transactional
    public ProjetoResponseDto atualizar(Long id, ProjetoUpdateDto dto) {
        Projeto projeto = buscarProjeto(id);
        boolean encerrando = projeto.getSituacao() != ProjetoStatus.ENCERRADO
                && dto.situacao() == ProjetoStatus.ENCERRADO;
        validarPeriodo(projeto, dto);
        if (dto.situacao() != null) {
            statusTransitionPolicy.validar(projeto.getSituacao(), dto.situacao());
        }
        ProjetoStatus situacaoFinal = dto.situacao() != null ? dto.situacao() : projeto.getSituacao();
        atualizarMembrosRelacionados(projeto, dto, situacaoFinal);
        projetoMapper.updateEntity(dto, projeto);
        if (dto.situacao() != null) projeto.setSituacao(dto.situacao());
        if (encerrando) projeto.setDataFimFinal(LocalDate.now(ZoneId.systemDefault()));
        return montarResposta(projeto);
    }

    @Transactional
    public void excluir(Long id) {
        Projeto projeto = buscarProjeto(id);
        if (SITUACOES_NAO_EXCLUIVEIS.contains(projeto.getSituacao())) {
            throw new ProjetoStatusExclusaoException(projeto.getSituacao());
        }
        projeto.setSituacao(ProjetoStatus.CANCELADO);
    }

    private Projeto buscarProjeto(Long id) {
        return projetoRepository.findById(id).orElseThrow(ProjetoNaoEncontradoException::new);
    }

    private Map<Long, MembroResponseDto> buscarMembros(Long gerenteId, Set<Long> membrosIds) {
        Set<Long> ids = new LinkedHashSet<>(membrosIds);
        ids.add(gerenteId);
        Map<Long, MembroResponseDto> encontrados = new LinkedHashMap<>();
        ids.forEach(id -> membroClient.buscarPorId(id)
                .ifPresent(membro -> encontrados.put(id, membro)));
        return encontrados;
    }

    private void validarMembrosRelacionados(Long gerenteId, Set<Long> membrosIds,
                                             Map<Long, MembroResponseDto> membros) {
        List<Violation> violations = new ArrayList<>();
        boolean naoEncontrado = false;
        MembroResponseDto gerente = membros.get(gerenteId);
        if (gerente == null) {
            naoEncontrado = true;
            violations.add(new Violation("gerenteId", "Gerente não encontrado: " + gerenteId));
        } else if (gerente.atribuicao() != AtribuicaoMembro.GERENTE) {
            violations.add(new Violation("gerenteId", "Membro informado não possui atribuição de gerente"));
        }
        Set<Long> ausentes = membrosIds.stream().filter(id -> !membros.containsKey(id))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!ausentes.isEmpty()) {
            naoEncontrado = true;
            violations.add(new Violation("membrosIds", "Membros não encontrados: " + ausentes));
        }
        Set<Long> naoFuncionarios = membrosIds.stream().filter(membros::containsKey)
                .filter(id -> membros.get(id).atribuicao() != AtribuicaoMembro.FUNCIONARIO)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!naoFuncionarios.isEmpty()) {
            violations.add(new Violation("membrosIds",
                    "Membros não possuem atribuição de funcionário: " + naoFuncionarios));
        }
        if (!violations.isEmpty()) {
            throw new ProjetoMembrosInvalidosException(
                    naoEncontrado ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST,
                    "Membros relacionados ao projeto sao inválidos", violations);
        }
    }

    private void validarPeriodo(Projeto projeto, ProjetoUpdateDto dto) {
        if (dto.dataInicio() != null
                && dto.dataInicio().isBefore(LocalDate.now(ZoneId.systemDefault()))) {
            throw new ProjetoPeriodoInvalidoException(
                    "dataInicio",
                    "Data de inicio deve ser igual ou posterior a data atual");
        }
        LocalDate inicio = dto.dataInicio() != null ? dto.dataInicio() : projeto.getDataInicio();
        LocalDate fim = dto.dataFimPrevisao() != null ? dto.dataFimPrevisao() : projeto.getDataFimPrevisao();
        if (fim.isBefore(inicio)) throw new ProjetoPeriodoInvalidoException();
    }

    private void atualizarMembrosRelacionados(Projeto projeto, ProjetoUpdateDto dto,
                                               ProjetoStatus situacaoFinal) {
        if (dto.gerenteId() == null && dto.membrosIds() == null) return;
        Long gerenteId = dto.gerenteId() != null ? dto.gerenteId() : projeto.getGerenteId();
        Set<Long> membrosIds = dto.membrosIds() != null ? dto.membrosIds() : projeto.getMembrosIds();
        Map<Long, MembroResponseDto> membros = buscarMembros(gerenteId, membrosIds);
        validarMembrosRelacionados(gerenteId, membrosIds, membros);
        validarLimiteProjetosAtivos(membrosIds, projeto.getId(), situacaoFinal);
        projeto.setGerenteId(gerenteId);
        projeto.setMembrosIds(new LinkedHashSet<>(membrosIds));
    }

    private void validarLimiteProjetosAtivos(Set<Long> membrosIds, Long projetoId,
                                              ProjetoStatus situacaoFinal) {
        if (SITUACOES_INATIVAS.contains(situacaoFinal)) return;
        membroAlocacaoLockRepository.bloquearEmOrdem(membrosIds);
        List<MembroProjetosAtivosProjection> quantidades = projetoId == null
                ? projetoRepository.contarProjetosAtivosPorMembro(membrosIds, SITUACOES_INATIVAS)
                : projetoRepository.contarOutrosProjetosAtivosPorMembro(
                        membrosIds, projetoId, SITUACOES_INATIVAS);
        Set<Long> noLimite = quantidades.stream()
                .filter(item -> item.getQuantidadeProjetos() >= LIMITE_PROJETOS_ATIVOS_POR_MEMBRO)
                .map(MembroProjetosAtivosProjection::getMembroId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!noLimite.isEmpty()) throw new ProjetoLimiteMembrosException(noLimite);
    }

    private ProjetoResponseDto montarResposta(Projeto projeto) {
        Map<Long, MembroResponseDto> membros = buscarMembros(projeto.getGerenteId(), projeto.getMembrosIds());
        validarMembrosRelacionados(projeto.getGerenteId(), projeto.getMembrosIds(), membros);
        return montarResposta(projeto, membros);
    }

    private ProjetoResponseDto montarResposta(Projeto projeto, Map<Long, MembroResponseDto> membros) {
        Set<MembroResponseDto> equipe = projeto.getMembrosIds().stream().map(membros::get)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return projetoMapper.toResponseDto(projeto, membros.get(projeto.getGerenteId()), equipe);
    }
}
