package com.desafio_java.desafio_java.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "projeto")
public class Projeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "datainicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "datafimprevisao", nullable = false)
    private LocalDate dataFimPrevisao;

    @Column(name = "datafimfinal")
    private LocalDate dataFimFinal;

    @Column(name = "orcamento", nullable = false, precision = 19, scale = 2)
    private BigDecimal orcamento;

    @Column(name = "descricao", nullable = false, length = 2000)
    private String descricao;

    @Column(name = "idgerente", nullable = false)
    private Long gerenteId;

    @Column(name = "situacao", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ProjetoStatus situacao;

    @ElementCollection
    @CollectionTable(
            name = "projetomembro",
            joinColumns = @JoinColumn(name = "idprojeto")
    )
    @Column(name = "idmembro", nullable = false)
    private Set<Long> membrosIds = new HashSet<>();
}
