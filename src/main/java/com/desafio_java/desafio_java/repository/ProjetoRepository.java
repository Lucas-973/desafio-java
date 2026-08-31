package com.desafio_java.desafio_java.repository;

import com.desafio_java.desafio_java.entity.Projeto;
import com.desafio_java.desafio_java.entity.ProjetoStatus;
import com.desafio_java.desafio_java.repository.projection.MembroProjetosAtivosProjection;
import com.desafio_java.desafio_java.repository.projection.ProjetosPorStatusProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ProjetoRepository extends JpaRepository<Projeto, Long>, JpaSpecificationExecutor<Projeto> {

    @Query("""
            SELECT p.situacao AS status,
                   COUNT(p.id) AS quantidadeProjetos,
                   SUM(p.orcamento) AS totalOrcado
            FROM Projeto p
            GROUP BY p.situacao
            """)
    List<ProjetosPorStatusProjection> resumirProjetosPorStatus();

    @Query("""
            SELECT p.dataInicio, p.dataFimFinal
            FROM Projeto p
            WHERE p.situacao = :status
              AND p.dataFimFinal IS NOT NULL
            """)
    List<Object[]> buscarPeriodosPorStatus(@Param("status") ProjetoStatus status);

    @Query("""
            SELECT COUNT(DISTINCT membroId)
            FROM Projeto p
            JOIN p.membrosIds membroId
            """)
    Long contarMembrosUnicosAlocados();

    @Query("""
            SELECT membro AS membroId,
                   COUNT(projeto.id) AS quantidadeProjetos
            FROM Projeto projeto
            JOIN projeto.membrosIds membro
            WHERE membro IN :membrosIds
              AND projeto.situacao NOT IN :situacoesInativas
            GROUP BY membro
            """)
    List<MembroProjetosAtivosProjection> contarProjetosAtivosPorMembro(
            @Param("membrosIds") Collection<Long> membrosIds,
            @Param("situacoesInativas") Collection<ProjetoStatus> situacoesInativas
    );

    @Query("""
            SELECT membro AS membroId,
                   COUNT(projeto.id) AS quantidadeProjetos
            FROM Projeto projeto
            JOIN projeto.membrosIds membro
            WHERE membro IN :membrosIds
              AND projeto.id <> :projetoId
              AND projeto.situacao NOT IN :situacoesInativas
            GROUP BY membro
            """)
    List<MembroProjetosAtivosProjection> contarOutrosProjetosAtivosPorMembro(
            @Param("membrosIds") Collection<Long> membrosIds,
            @Param("projetoId") Long projetoId,
            @Param("situacoesInativas") Collection<ProjetoStatus> situacoesInativas
    );
}
