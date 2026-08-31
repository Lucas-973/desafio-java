package com.desafio_java.desafio_java.controller;

import com.desafio_java.desafio_java.dto.ProjetoCreateDto;
import com.desafio_java.desafio_java.dto.ProjetoFiltroDto;
import com.desafio_java.desafio_java.dto.ProjetoResponseDto;
import com.desafio_java.desafio_java.dto.ProjetoUpdateDto;
import com.desafio_java.desafio_java.service.ProjetoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projetos")
@RequiredArgsConstructor
@Tag(name = "Projetos", description = "Cadastro e gerenciamento dos projetos do portfólio")
public class ProjetoController {

    private final ProjetoService projetoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar projeto")
    @ApiResponse(responseCode = "201", description = "Projeto criado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Gerente ou membro não encontrado")
    @ApiResponse(responseCode = "409", description = "Regra de negócio violada")
    public ProjetoResponseDto criar(@Valid @RequestBody ProjetoCreateDto dto) {
        return projetoService.criar(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar projeto por identificador")
    @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    public ProjetoResponseDto buscarPorId(
            @PathVariable @Positive(message = "Identificador do projeto deve ser maior que zero") Long id
    ) {
        return projetoService.buscarPorId(id);
    }

    @GetMapping
    public Page<ProjetoResponseDto> buscarTodos(
            @Valid @ModelAttribute ProjetoFiltroDto filtro,
            @PageableDefault Pageable pageable
    ) {
        return projetoService.buscarTodos(filtro, pageable);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar parcialmente um projeto")
    @ApiResponse(responseCode = "200", description = "Projeto atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Projeto, gerente ou membro não encontrado")
    @ApiResponse(responseCode = "409", description = "Regra de negócio violada")
    public ProjetoResponseDto atualizar(
            @PathVariable @Positive(message = "Identificador do projeto deve ser maior que zero") Long id,
            @Valid @RequestBody ProjetoUpdateDto dto
    ) {
        return projetoService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir projeto")
    @ApiResponse(responseCode = "204", description = "Projeto excluído")
    @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    @ApiResponse(responseCode = "409", description = "Situação do projeto não permite exclusão")
    public void excluir(
            @PathVariable @Positive(message = "Identificador do projeto deve ser maior que zero") Long id
    ) {
        projetoService.excluir(id);
    }
}
