package com.desafio_java.mock_membros.controller;

import com.desafio_java.mock_membros.dto.MembroCreateDto;
import com.desafio_java.mock_membros.dto.MembroResponseDto;
import com.desafio_java.mock_membros.service.MembroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/membros")
public class MembroController {

    private final MembroService membroService;

    public MembroController(MembroService membroService) {
        this.membroService = membroService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MembroResponseDto criar(@Valid @RequestBody MembroCreateDto dto) {
        return membroService.criar(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MembroResponseDto> buscarPorId(@PathVariable Long id) {
        return membroService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<MembroResponseDto> listar(@RequestParam(required = false) Set<Long> ids) {
        return ids == null ? membroService.listar() : membroService.buscarPorIds(ids);
    }
}
