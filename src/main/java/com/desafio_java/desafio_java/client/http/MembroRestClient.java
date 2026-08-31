package com.desafio_java.desafio_java.client.http;

import com.desafio_java.desafio_java.client.MembroClient;
import com.desafio_java.desafio_java.client.dto.MembroResponseDto;
import com.desafio_java.desafio_java.exception.MembroApiIndisponivelException;
import com.desafio_java.desafio_java.exception.MembroApiRespostaInvalidaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MembroRestClient implements MembroClient {

    private static final ParameterizedTypeReference<List<MembroResponseDto>> LISTA_MEMBROS_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;

    public MembroRestClient(
            @Value("${integrations.membros.base-url}") String baseUrl,
            @Value("${integrations.membros.connect-timeout}") Duration connectTimeout,
            @Value("${integrations.membros.read-timeout}") Duration readTimeout
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public Optional<MembroResponseDto> buscarPorId(Long id) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri("/api/membros/{id}", id)
                    .retrieve()
                    .body(MembroResponseDto.class));
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw traduzirErro(exception);
        } catch (RuntimeException exception) {
            throw new MembroApiIndisponivelException(exception);
        }
    }

    @Override
    public Map<Long, MembroResponseDto> buscarPorIds(Set<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        try {
            List<MembroResponseDto> membros = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/membros")
                            .queryParam("ids", ids)
                            .build())
                    .retrieve()
                    .body(LISTA_MEMBROS_TYPE);
            if (membros == null) return Map.of();
            return membros.stream().collect(Collectors.toMap(
                    MembroResponseDto::id,
                    Function.identity(),
                    (primeiro, ignorado) -> primeiro,
                    LinkedHashMap::new));
        } catch (RestClientResponseException exception) {
            throw traduzirErro(exception);
        } catch (RuntimeException exception) {
            throw new MembroApiIndisponivelException(exception);
        }
    }

    private RuntimeException traduzirErro(RestClientResponseException exception) {
        if (exception.getStatusCode().is4xxClientError()) {
            return new MembroApiRespostaInvalidaException(exception);
        }
        return new MembroApiIndisponivelException(exception);
    }
}
