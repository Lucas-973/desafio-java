package com.desafio_java.desafio_java.service;

import com.desafio_java.desafio_java.dto.PortfolioRelatorioResponseDto;
import com.desafio_java.desafio_java.entity.ProjetoStatus;
import com.desafio_java.desafio_java.repository.ProjetoRepository;
import com.desafio_java.desafio_java.repository.projection.ProjetosPorStatusProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioRelatorioServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;
    @InjectMocks
    private PortfolioRelatorioService service;

    @Test
    void deveGerarRelatorioCompletoEPreencherStatusSemProjetos() {
        ProjetosPorStatusProjection resumo = mock(ProjetosPorStatusProjection.class);
        when(resumo.getStatus()).thenReturn(ProjetoStatus.EM_ANALISE);
        when(resumo.getQuantidadeProjetos()).thenReturn(2L);
        when(resumo.getTotalOrcado()).thenReturn(new BigDecimal("3000.00"));
        when(projetoRepository.resumirProjetosPorStatus()).thenReturn(List.of(resumo));
        when(projetoRepository.buscarPeriodosPorStatus(ProjetoStatus.ENCERRADO))
                .thenReturn(List.of(
                        new Object[]{LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 11)},
                        new Object[]{LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 3)}
                ));
        when(projetoRepository.contarMembrosUnicosAlocados()).thenReturn(7L);

        PortfolioRelatorioResponseDto relatorio = service.gerar();

        assertEquals(ProjetoStatus.values().length, relatorio.projetosPorStatus().size());
        assertEquals(2L, relatorio.projetosPorStatus().getFirst().quantidadeProjetos());
        assertEquals(new BigDecimal("3000.00"),
                relatorio.projetosPorStatus().getFirst().totalOrcado());
        assertEquals(new BigDecimal("20.00"),
                relatorio.mediaDuracaoProjetosEncerradosDias());
        assertEquals(7L, relatorio.totalMembrosUnicosAlocados());
        assertTrue(relatorio.projetosPorStatus().stream()
                .filter(item -> item.status() != ProjetoStatus.EM_ANALISE)
                .allMatch(item -> item.quantidadeProjetos() == 0L));
    }

    @Test
    void deveRetornarMediaZeroSemProjetosEncerrados() {
        when(projetoRepository.resumirProjetosPorStatus()).thenReturn(List.of());
        when(projetoRepository.buscarPeriodosPorStatus(ProjetoStatus.ENCERRADO))
                .thenReturn(List.of());
        when(projetoRepository.contarMembrosUnicosAlocados()).thenReturn(0L);

        assertEquals(new BigDecimal("0.00"),
                service.gerar().mediaDuracaoProjetosEncerradosDias());
    }
}
