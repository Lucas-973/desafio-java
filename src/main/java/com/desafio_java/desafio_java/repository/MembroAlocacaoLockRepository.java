package com.desafio_java.desafio_java.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class MembroAlocacaoLockRepository {

    private final JdbcTemplate jdbcTemplate;

    public MembroAlocacaoLockRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void bloquearEmOrdem(Set<Long> membrosIds) {
        membrosIds.stream()
                .sorted()
                .forEach(this::bloquear);
    }

    private void bloquear(Long membroId) {
        jdbcTemplate.queryForObject(
                "SELECT pg_advisory_xact_lock(?)",
                Object.class,
                membroId);
    }
}
