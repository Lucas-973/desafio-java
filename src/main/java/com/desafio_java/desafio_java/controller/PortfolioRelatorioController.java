package com.desafio_java.desafio_java.controller;

import com.desafio_java.desafio_java.dto.PortfolioRelatorioResponseDto;
import com.desafio_java.desafio_java.service.PortfolioRelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Relatório com indicadores consolidados do portfólio de projetos")
public class PortfolioRelatorioController {

    private final PortfolioRelatorioService portfolioRelatorioService;

    @GetMapping("/portfolio")
    @Operation(summary = "Gerar relatório do portfólio")
    public PortfolioRelatorioResponseDto gerarRelatorioPortfolio() {
        return portfolioRelatorioService.gerar();
    }
}
