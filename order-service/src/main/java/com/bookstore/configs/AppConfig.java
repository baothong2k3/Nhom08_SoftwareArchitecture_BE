package com.bookstore.configs;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(30_000)) // Connection timeout
                .setResponseTimeout(Timeout.ofMilliseconds(30_000)) // Response timeout
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        return builder
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
                .build();
    }
}