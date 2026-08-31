package com.desafio_java.mock_membros.service;

import com.desafio_java.mock_membros.dto.MembroCreateDto;
import com.desafio_java.mock_membros.dto.MembroResponseDto;
import com.desafio_java.mock_membros.model.Atribuicao;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MembroService {

    private final AtomicLong sequence = new AtomicLong();
    private final ConcurrentMap<Long, MembroResponseDto> membros = new ConcurrentHashMap<>();

    public MembroService() {
        adicionarInicial(1L, "Wagner Parisoto", Atribuicao.GERENTE);
        adicionarInicial(2L, "Joel Benelli", Atribuicao.GERENTE);
        adicionarInicial(3L, "Lucas Teixeira", Atribuicao.FUNCIONARIO);
        adicionarInicial(4L, "Thayane Martins", Atribuicao.FUNCIONARIO);
        adicionarInicial(5L, "Eduardo Simas", Atribuicao.FUNCIONARIO);
        adicionarInicial(6L, "João Albieri", Atribuicao.FUNCIONARIO);
    }

    public MembroResponseDto criar(MembroCreateDto dto) {
        long id = sequence.incrementAndGet();
        MembroResponseDto membro = new MembroResponseDto(id, dto.nome(), dto.atribuicao());
        membros.put(id, membro);
        return membro;
    }

    public Optional<MembroResponseDto> buscarPorId(Long id) {
        return Optional.ofNullable(membros.get(id));
    }

    public List<MembroResponseDto> listar() {
        return membros.values().stream()
                .sorted(Comparator.comparing(MembroResponseDto::id))
                .toList();
    }

    public List<MembroResponseDto> buscarPorIds(Set<Long> ids) {
        return ids.stream()
                .map(membros::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(MembroResponseDto::id))
                .toList();
    }

    private void adicionarInicial(
            Long id,
            String nome,
            Atribuicao atribuicao
    ) {
        membros.put(id, new MembroResponseDto(id, nome, atribuicao));
        sequence.set(Math.max(sequence.get(), id));
    }
}
