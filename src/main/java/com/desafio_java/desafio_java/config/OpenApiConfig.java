package com.desafio_java.desafio_java.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(
        info = @Info(
                title = "API de Gestão de Portfólio",
                version = "v1",
                description = "API REST para gerenciamento de projetos e indicadores do portfólio.",
                contact = @Contact(name = "Desafio Java")
        ),
        security = @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
)
@SecurityScheme(
        name = OpenApiConfig.BASIC_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "basic",
        description = "Autenticação HTTP Basic configurada por APP_USERNAME e APP_PASSWORD."
)
public class OpenApiConfig {

    public static final String BASIC_AUTH = "basicAuth";

    private OpenApiConfig() {
    }
}
