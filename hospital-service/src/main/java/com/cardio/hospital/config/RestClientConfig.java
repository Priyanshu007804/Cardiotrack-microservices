package com.cardio.hospital.config;

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

    @Value("${services.doctor-service.url:http://DOCTOR-SERVICE}")
    private String doctorServiceUrl;

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
    public RestClient patientServiceRestClient(
            RestClient.Builder plainRestClientBuilder,
            @LoadBalanced RestClient.Builder loadBalancedRestClientBuilder) {

        boolean isServiceId = patientServiceUrl != null && (
                patientServiceUrl.startsWith("http://PATIENT-SERVICE")
                || (!patientServiceUrl.contains(".") && !patientServiceUrl.contains("://"))
        );

        RestClient.Builder builderToUse = isServiceId ? loadBalancedRestClientBuilder : plainRestClientBuilder;

        return builderToUse.clone()
                .baseUrl(patientServiceUrl)
                .requestInterceptor((request, body, execution) -> {
                    forwardAuthorizationHeader(request);
                    return execution.execute(request, body);
                })
                .build();
    }

    @Bean
    public RestClient doctorServiceRestClient(
            RestClient.Builder plainRestClientBuilder,
            @LoadBalanced RestClient.Builder loadBalancedRestClientBuilder) {

        boolean isServiceId = doctorServiceUrl != null && (
                doctorServiceUrl.startsWith("http://DOCTOR-SERVICE")
                || (!doctorServiceUrl.contains(".") && !doctorServiceUrl.contains("://"))
        );

        RestClient.Builder builderToUse = isServiceId ? loadBalancedRestClientBuilder : plainRestClientBuilder;

        return builderToUse.clone()
                .baseUrl(doctorServiceUrl)
                .requestInterceptor((request, body, execution) -> {
                    forwardAuthorizationHeader(request);
                    return execution.execute(request, body);
                })
                .build();
    }
}

