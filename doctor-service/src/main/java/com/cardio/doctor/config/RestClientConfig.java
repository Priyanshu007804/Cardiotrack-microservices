package com.cardio.doctor.config;

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

    @Bean
    public RestClient patientServiceRestClient() {
        return RestClient.builder()
                .baseUrl(patientServiceUrl)
                .requestInterceptor((request, body, execution) -> {
                    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        String authHeader = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                        if (authHeader != null && !authHeader.isBlank()) {
                            request.getHeaders().set(HttpHeaders.AUTHORIZATION, authHeader);
                        }
                    }
                    return execution.execute(request, body);
                })
                .build();
    }
}
