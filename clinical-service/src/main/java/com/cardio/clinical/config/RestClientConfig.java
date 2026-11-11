package com.cardio.clinical.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class RestClientConfig {

    @Value("${services.patient-service.url:http://localhost:8082}")
    private String patientServiceUrl;

    @Value("${services.doctor-service.url:http://localhost:8083}")
    private String doctorServiceUrl;

    private void forwardAuthorizationHeader(org.springframework.http.HttpRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String authHeader = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && !authHeader.isBlank()) {
                request.getHeaders().set(HttpHeaders.AUTHORIZATION, authHeader);
            }
        }
    }

    @Bean
    public RestClient patientServiceRestClient() {
        return RestClient.builder()
                .baseUrl(patientServiceUrl)
                .requestInterceptor((request, body, execution) -> {
                    forwardAuthorizationHeader(request);
                    return execution.execute(request, body);
                })
                .build();
    }

    @Bean
    public RestClient doctorServiceRestClient() {
        return RestClient.builder()
                .baseUrl(doctorServiceUrl)
                .requestInterceptor((request, body, execution) -> {
                    forwardAuthorizationHeader(request);
                    return execution.execute(request, body);
                })
                .build();
    }
}

