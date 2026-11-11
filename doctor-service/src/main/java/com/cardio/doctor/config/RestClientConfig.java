package com.cardio.doctor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class RestClientConfig {

    @Value("${services.patient-service.url:http://PATIENT-SERVICE}")
    private String patientServiceUrl;

    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient patientServiceRestClient(@LoadBalanced RestClient.Builder loadBalancedRestClientBuilder) {
        return loadBalancedRestClientBuilder.clone()
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
