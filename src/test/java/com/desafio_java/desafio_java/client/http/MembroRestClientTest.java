package com.desafio_java.desafio_java.client.http;

import com.desafio_java.desafio_java.exception.MembroApiIndisponivelException;
import com.desafio_java.desafio_java.exception.MembroApiRespostaInvalidaException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MembroRestClientTest {

    private HttpServer server;
    private MembroRestClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.start();
        client = new MembroRestClient(
                "http://localhost:" + server.getAddress().getPort(),
                Duration.ofSeconds(1),
                Duration.ofSeconds(1));
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void deveRetornarVazioQuandoMembroNaoExiste() {
        server.createContext("/api/membros/99", exchange -> responder(exchange, 404, ""));

        assertTrue(client.buscarPorId(99L).isEmpty());
    }

    @Test
    void deveDiferenciarErroDeRequisicaoAoServicoExterno() {
        server.createContext("/api/membros/1", exchange -> responder(exchange, 400, "{}"));

        assertThrows(MembroApiRespostaInvalidaException.class, () -> client.buscarPorId(1L));
    }

    @Test
    void deveTratarErroDoServicoExternoComoIndisponibilidade() {
        server.createContext("/api/membros/1", exchange -> responder(exchange, 500, "{}"));

        assertThrows(MembroApiIndisponivelException.class, () -> client.buscarPorId(1L));
    }

    @Test
    void deveBuscarMembrosEmLote() {
        server.createContext("/api/membros", exchange -> responder(exchange, 200, """
                [
                  {"id":1,"nome":"Gerente","atribuicao":"GERENTE"},
                  {"id":2,"nome":"Funcionario","atribuicao":"FUNCIONARIO"}
                ]
                """));

        var membros = client.buscarPorIds(Set.of(1L, 2L));

        assertEquals(Set.of(1L, 2L), membros.keySet());
    }

    private void responder(HttpExchange exchange, int status, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
