package com.cardio.doctor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.cardio.doctor.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/actuator/health",
                                "/error")
                        .permitAll()

                        // Admin-only assignment creation and deletion
                        .requestMatchers(HttpMethod.POST, "/api/doctor-assignments/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/doctor-assignments/**")
                        .hasRole("ADMIN")

                        // Patient self-service to view their own assigned doctors
                        .requestMatchers(HttpMethod.GET, "/api/doctor-assignments/my-doctors")
                        .hasRole("PATIENT")

                        // Admin, Staff, and Doctors can view general assignments
                        .requestMatchers(HttpMethod.GET, "/api/doctor-assignments/**")
                        .hasAnyRole("ADMIN", "STAFF", "DOCTOR")

                        // Other endpoints require authentication.
                        // Fine-grained permissions are handled by @PreAuthorize.
                        .anyRequest().authenticated())

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
