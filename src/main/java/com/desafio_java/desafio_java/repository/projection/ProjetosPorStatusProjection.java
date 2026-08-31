package com.desafio_java.desafio_java.repository.projection;

import com.desafio_java.desafio_java.entity.ProjetoStatus;

import java.math.BigDecimal;

public interface ProjetosPorStatusProjection {

    ProjetoStatus getStatus();

    Long getQuantidadeProjetos();

    BigDecimal getTotalOrcado();
}
