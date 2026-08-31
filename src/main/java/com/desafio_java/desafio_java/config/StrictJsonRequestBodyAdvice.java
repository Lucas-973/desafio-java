package com.desafio_java.desafio_java.config;

import com.desafio_java.desafio_java.exception.UnknownJsonFieldsException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ControllerAdvice
@RequiredArgsConstructor
@NullMarked
public class StrictJsonRequestBodyAdvice extends RequestBodyAdviceAdapter {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(
            MethodParameter methodParameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return targetType instanceof Class<?> targetClass && targetClass.isRecord();
    }

    @Override
    public HttpInputMessage beforeBodyRead(
            HttpInputMessage inputMessage,
            MethodParameter parameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) throws IOException {
        byte[] body = inputMessage.getBody().readAllBytes();
        HttpInputMessage reusableInputMessage = new ReusableHttpInputMessage(
                new ByteArrayResource(body),
                inputMessage.getHeaders()
        );

        try {
            JsonNode json = objectMapper.readTree(body);
            validarCampos(json, (Class<?>) targetType);
        } catch (JacksonException _) {
            return reusableInputMessage;
        }

        return reusableInputMessage;
    }

    private void validarCampos(JsonNode json, Class<?> targetClass) {
        if (!json.isObject()) {
            return;
        }

        Set<String> camposPermitidos = Arrays.stream(targetClass.getRecordComponents())
                .map(RecordComponent::getName)
                .collect(Collectors.toSet());
        List<String> camposDesconhecidos = json.propertyNames().stream()
                .filter(field -> !camposPermitidos.contains(field))
                .toList();

        if (!camposDesconhecidos.isEmpty()) {
            throw new UnknownJsonFieldsException(camposDesconhecidos);
        }
    }

    private record ReusableHttpInputMessage(
            ByteArrayResource bodyResource,
            HttpHeaders headers
    ) implements HttpInputMessage {

        @Override
        public InputStream getBody() throws IOException {
            return bodyResource.getInputStream();
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }
    }
}
