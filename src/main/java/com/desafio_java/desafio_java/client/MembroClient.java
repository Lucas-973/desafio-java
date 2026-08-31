package com.desafio_java.desafio_java.client;

import com.desafio_java.desafio_java.client.dto.MembroResponseDto;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface MembroClient {

    Optional<MembroResponseDto> buscarPorId(Long id);

    Map<Long, MembroResponseDto> buscarPorIds(Set<Long> ids);
}
