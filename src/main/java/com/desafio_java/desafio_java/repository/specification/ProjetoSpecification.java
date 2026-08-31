package com.desafio_java.desafio_java.repository.specification;

import com.desafio_java.desafio_java.dto.ProjetoFiltroDto;
import com.desafio_java.desafio_java.entity.Projeto;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;
import java.util.stream.Stream;

public final class ProjetoSpecification {

    private ProjetoSpecification() {
    }

    public static Specification<Projeto> comFiltros(ProjetoFiltroDto filtro) {
        return Stream.of(
                nomeContem(filtro.nome()),
                situacaoIgual(filtro),
                gerenteIgual(filtro),
                dataInicioMaiorOuIgual(filtro),
                dataInicioMenorOuIgual(filtro),
                orcamentoMaiorOuIgual(filtro),
                orcamentoMenorOuIgual(filtro))
                .filter(java.util.Objects::nonNull)
                .reduce(Specification.unrestricted(), Specification::and);
    }

    private static Specification<Projeto> nomeContem(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        String termo = "%" + nome.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, _, builder) -> builder.like(builder.lower(root.get("nome")), termo);
    }

    private static Specification<Projeto> situacaoIgual(ProjetoFiltroDto filtro) {
        return filtro.situacao() == null
                ? null
                : (root, _, builder) -> builder.equal(root.get("situacao"), filtro.situacao());
    }

    private static Specification<Projeto> gerenteIgual(ProjetoFiltroDto filtro) {
        return filtro.gerenteId() == null
                ? null
                : (root, _, builder) -> builder.equal(root.get("gerenteId"), filtro.gerenteId());
    }

    private static Specification<Projeto> dataInicioMaiorOuIgual(ProjetoFiltroDto filtro) {
        return filtro.dataInicioDe() == null
                ? null
                : (root, _, builder) -> builder.greaterThanOrEqualTo(root.get("dataInicio"), filtro.dataInicioDe());
    }

    private static Specification<Projeto> dataInicioMenorOuIgual(ProjetoFiltroDto filtro) {
        return filtro.dataInicioAte() == null
                ? null
                : (root, _, builder) -> builder.lessThanOrEqualTo(root.get("dataInicio"), filtro.dataInicioAte());
    }

    private static Specification<Projeto> orcamentoMaiorOuIgual(ProjetoFiltroDto filtro) {
        return filtro.orcamentoMinimo() == null
                ? null
                : (root, _, builder) -> builder.greaterThanOrEqualTo(root.get("orcamento"), filtro.orcamentoMinimo());
    }

    private static Specification<Projeto> orcamentoMenorOuIgual(ProjetoFiltroDto filtro) {
        return filtro.orcamentoMaximo() == null
                ? null
                : (root, _, builder) -> builder.lessThanOrEqualTo(root.get("orcamento"), filtro.orcamentoMaximo());
    }
}
