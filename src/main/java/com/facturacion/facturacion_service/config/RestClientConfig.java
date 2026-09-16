package com.facturacion.facturacion_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(
            @Value("${document-service.url}") String documentServiceUrl) {

        return RestClient.builder()
                .baseUrl(documentServiceUrl)
                .build();
    }
}