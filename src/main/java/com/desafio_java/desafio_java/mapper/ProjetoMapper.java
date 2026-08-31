package com.desafio_java.desafio_java.mapper;

import com.desafio_java.desafio_java.client.dto.MembroResponseDto;
import com.desafio_java.desafio_java.dto.ProjetoCreateDto;
import com.desafio_java.desafio_java.dto.ProjetoResponseDto;
import com.desafio_java.desafio_java.dto.ProjetoUpdateDto;
import com.desafio_java.desafio_java.entity.Projeto;
import com.desafio_java.desafio_java.entity.ProjetoStatus;
import com.desafio_java.desafio_java.service.ProjetoRiscoCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ProjetoMapper {

    private final ProjetoRiscoCalculator projetoRiscoCalculator;

    public Projeto toEntity(ProjetoCreateDto dto) {
        Projeto projeto = new Projeto();
        projeto.setNome(dto.nome());
        projeto.setDataInicio(dto.dataInicio());
        projeto.setDataFimPrevisao(dto.dataFimPrevisao());
        projeto.setOrcamento(dto.orcamento());
        projeto.setDescricao(dto.descricao());
        projeto.setGerenteId(dto.gerenteId());
        projeto.setSituacao(ProjetoStatus.EM_ANALISE);
        projeto.setMembrosIds(new LinkedHashSet<>(dto.membrosIds()));

        return projeto;
    }

    public ProjetoResponseDto toResponseDto(
            Projeto projeto,
            MembroResponseDto gerente,
            Set<MembroResponseDto> membros
    ) {
        return new ProjetoResponseDto(
                projeto.getId(),
                projeto.getNome(),
                projeto.getDataInicio(),
                projeto.getDataFimPrevisao(),
                projeto.getDataFimFinal(),
                projeto.getOrcamento(),
                projeto.getDescricao(),
                gerente,
                projeto.getSituacao(),
                projetoRiscoCalculator.calcular(
                        projeto.getOrcamento(),
                        projeto.getDataInicio(),
                        projeto.getDataFimPrevisao()
                ),
                membros
        );
    }

    public void updateEntity(ProjetoUpdateDto dto, Projeto projeto) {
        if (dto.nome() != null) {
            projeto.setNome(dto.nome());
        }
        if (dto.dataInicio() != null) {
            projeto.setDataInicio(dto.dataInicio());
        }
        if (dto.dataFimPrevisao() != null) {
            projeto.setDataFimPrevisao(dto.dataFimPrevisao());
        }
        if (dto.orcamento() != null) {
            projeto.setOrcamento(dto.orcamento());
        }
        if (dto.descricao() != null) {
            projeto.setDescricao(dto.descricao());
        }
    }
}
