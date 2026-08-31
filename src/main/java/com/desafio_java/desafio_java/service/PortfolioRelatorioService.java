package com.desafio_java.desafio_java.service;

import com.desafio_java.desafio_java.dto.PortfolioRelatorioResponseDto;
import com.desafio_java.desafio_java.dto.PortfolioRelatorioResponseDto.ProjetosPorStatusDto;
import com.desafio_java.desafio_java.entity.ProjetoStatus;
import com.desafio_java.desafio_java.repository.ProjetoRepository;
import com.desafio_java.desafio_java.repository.projection.ProjetosPorStatusProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioRelatorioService {

    private static final int ESCALA_MEDIA_DIAS = 2;

    private final ProjetoRepository projetoRepository;

    @Transactional(readOnly = true)
    public PortfolioRelatorioResponseDto gerar() {
        return new PortfolioRelatorioResponseDto(
                gerarResumoPorStatus(),
                calcularMediaDuracaoProjetosEncerrados(),
                projetoRepository.contarMembrosUnicosAlocados()
        );
    }

    private List<ProjetosPorStatusDto> gerarResumoPorStatus() {
        Map<ProjetoStatus, ProjetosPorStatusProjection> resumoPorStatus =
                projetoRepository.resumirProjetosPorStatus().stream()
                        .collect(Collectors.toMap(
                                ProjetosPorStatusProjection::getStatus,
                                Function.identity(),
                                (existente, _) -> existente,
                                () -> new EnumMap<>(ProjetoStatus.class)
                        ));

        return Arrays.stream(ProjetoStatus.values())
                .map(status -> converterResumo(status, resumoPorStatus.get(status)))
                .toList();
    }

    private ProjetosPorStatusDto converterResumo(
            ProjetoStatus status,
            ProjetosPorStatusProjection resumo
    ) {
        if (resumo == null) {
            return new ProjetosPorStatusDto(status, 0L, BigDecimal.ZERO);
        }

        return new ProjetosPorStatusDto(
                status,
                resumo.getQuantidadeProjetos(),
                resumo.getTotalOrcado()
        );
    }

    private BigDecimal calcularMediaDuracaoProjetosEncerrados() {
        List<Object[]> periodos = projetoRepository.buscarPeriodosPorStatus(
                ProjetoStatus.ENCERRADO
        );

        if (periodos.isEmpty()) {
            return BigDecimal.ZERO.setScale(ESCALA_MEDIA_DIAS, RoundingMode.HALF_UP);
        }

        long totalDias = periodos.stream()
                .mapToLong(periodo -> ChronoUnit.DAYS.between(
                        (LocalDate) periodo[0],
                        (LocalDate) periodo[1]
                ))
                .sum();

        return BigDecimal.valueOf(totalDias)
                .divide(
                        BigDecimal.valueOf(periodos.size()),
                        ESCALA_MEDIA_DIAS,
                        RoundingMode.HALF_UP
                );
    }
}
