package com.desafio_java.desafio_java.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiDocumentationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void devePermitirAcessoPublicoAoOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"gerenteOuFuncionario\""))))
                .andExpect(content().string(not(containsString("\"periodoValido\""))))
                .andExpect(content().string(not(containsString("\"intervaloOrcamentoValido\""))));
    }

    @Test
    void devePermitirAcessoPublicoAoSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void deveManterEndpointsDeNegocioProtegidos() throws Exception {
        mockMvc.perform(get("/projetos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveIgnorarCsrfNasOperacoesDeEscritaDeProjetos() throws Exception {
        mockMvc.perform(post("/projetos")
                        .with(httpBasic("test", "test"))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/projetos/0")
                        .with(httpBasic("test", "test"))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/projetos/0")
                        .with(httpBasic("test", "test")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveManterCsrfAtivoForaDosEndpointsDeProjetos() throws Exception {
        mockMvc.perform(post("/relatorios/portfolio")
                        .with(httpBasic("test", "test")))
                .andExpect(status().isForbidden());
    }
}
